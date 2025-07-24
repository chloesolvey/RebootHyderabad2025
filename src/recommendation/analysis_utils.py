import re
import hashlib
from moz_sql_parser import parse

def analyze_sql(sql_text, query_stats=None):
    """
    Analyze SQL text using AST-based parsing and generate query-specific optimization recommendations.

    Args:
        sql_text (str): SQL string to analyze.
        query_stats (dict or None): Optional context such as frequency and average cost.

    Returns:
        list of str: Recommendations from analysis.
    """
    recommendations = []

    try:
        ast = parse(sql_text)
    except Exception:
        return ["Invalid SQL syntax; unable to parse for detailed analysis."]

    sql_upper = sql_text.upper()
    query_stats = query_stats or {}

    select_clause = ast.get('select')
    from_clause = ast.get('from')
    where_clause = ast.get('where')
    with_clause = ast.get('with')

    # 1. SELECT *
    if _has_select_star(select_clause):
        recommendations.append("Avoid SELECT *; specify only necessary columns to minimize data scanned.")

    # 2. WHERE clause presence
    if not where_clause:
        recommendations.append("No WHERE clause found; consider adding filters to reduce scanned data and improve performance.")

    # 3. JOIN counts and ON missing
    join_count, on_missing = _count_joins(from_clause) if from_clause else (0, False)
    if join_count > 0:
        recommendations.append(f"Query contains {join_count} JOIN(s); ensure join keys and conditions are optimal.")
    if on_missing:
        recommendations.append("One or more JOINs lack ON clause; this may cause Cartesian products and degrade performance.")

    # 4. Partition pruning filters
    where_cols = _extract_predicates_where(where_clause or {})
    if not _has_partition_filter(where_cols):
        recommendations.append("Add filters on partition columns (e.g., _PARTITIONTIME) to enable partition pruning.")

    # 5. Aggregations and GROUP BY
    has_group_by = 'groupby' in ast
    uses_aggregation = _has_aggregation(select_clause)
    if uses_aggregation and not has_group_by:
        recommendations.append("Aggregations used without GROUP BY; confirm correctness and consider adding GROUP BY clause.")

    # 6. ORDER BY presence
    if 'orderby' in ast:
        recommendations.append("ORDER BY found; consider adding LIMIT to reduce query cost.")

    # 7. Expensive functions (regex fallback)
    for func in ['REGEXP_EXTRACT', 'REGEXP_CONTAINS', 'ARRAY_AGG', 'UNNEST', 'WINDOW']:
        if func in sql_upper:
            recommendations.append(f"Use of expensive function {func}; filter data early before applying.")

    # 8. DISTINCT detection
    if ast.get('distinct') is True or 'DISTINCT' in sql_upper:
        recommendations.append("DISTINCT detected; consider pre-filtering data to reduce input size.")

    # 9. CTE usage: Recommend if any WITH clause exists
    if with_clause:
        recommendations.append("CTE (WITH statement) used; ensure it is optimized.")

    # 10. Window functions detection
    if _find_window_functions(select_clause):
        recommendations.append("Window functions detected; verify partitioning and ordering for performance.")

    # 11. UNION vs UNION ALL detection (text fallback)
    if 'UNION ' in sql_upper and 'UNION ALL' not in sql_upper:
        recommendations.append("Prefer UNION ALL over UNION if duplicate elimination is not required.")

    # 12-14. Regex fallback checks
    if re.search(r"CAST\s*\(.*\s+AS\s+STRING\s*\)", sql_text, re.IGNORECASE):
        recommendations.append("Casting to STRING in joins or filters can cause full scans; verify data types.")
    if re.search(r"(CAST|SUBSTR|CONCAT|COALESCE|IFNULL|CASE)", sql_text, re.IGNORECASE):
        recommendations.append("Computed columns detected; consider materializing or precomputing them.")

    # Broadened view detection regex
    if re.search(r"\bFROM\b[^\n;]*\b\w*_view\b", sql_text, re.IGNORECASE):
        recommendations.append("Query accesses views; flatten or materialize views if performance suffers.")

    # Early filtering before joins check
    join_keys = _extract_join_conditions(from_clause)
    if join_keys and where_cols and not join_keys.intersection(where_cols):
        recommendations.append(
            "No filtering on join keys detected before JOINs; consider pushing filters before joins "
            "to reduce join input size and improve performance."
        )

    # Query complexity estimation
    complexity_score = (
        2 * join_count
        + (1 if with_clause else 0)
        + (1 if _find_window_functions(select_clause) else 0)
        + (1 if uses_aggregation and not has_group_by else 0)
        + (1 if ast.get('distinct') is True or 'DISTINCT' in sql_upper else 0)
    )
    if complexity_score <= 2:
        complexity_desc = "Low"
    elif complexity_score <= 4:
        complexity_desc = "Medium"
    else:
        complexity_desc = "High"
    recommendations.append(f"Estimated query complexity level: {complexity_desc} (score={complexity_score}).")

    # Context-based cost/frequency recommendations
    freq = query_stats.get('frequency') if query_stats else None
    avg_cost = query_stats.get('average_cost') if query_stats else None
    if freq is not None and freq > 100:
        recommendations.append(f"Query runs frequently ({freq} times); consider optimization impact on workload.")
    if avg_cost is not None:
        avg_cost_gb = avg_cost / (1024 ** 3)
        if avg_cost_gb > 10:
            recommendations.append(f"Query processes large data volume ({avg_cost_gb:.2f} GB); consider partition pruning or other optimizations.")

    return recommendations

def _has_select_star(select_clause):
    if select_clause == '*':
        return True
    if isinstance(select_clause, list):
        for col in select_clause:
            if col == '*' or (isinstance(col, dict) and (col.get('value') == '*' or col.get('name') == '*')):
                return True
    if isinstance(select_clause, dict):
        for val in select_clause.values():
            if val == '*':
                return True
    return False

def _count_joins(from_clause):
    join_count = 0
    on_missing = False
    def rec(f):
        nonlocal join_count, on_missing
        if isinstance(f, dict):
            if 'join' in f:
                join_count += 1
                if 'on' not in f:
                    on_missing = True
                rec(f['join'])
            if 'value' in f:
                rec(f['value'])
            if 'args' in f and isinstance(f['args'], list):
                for arg in f['args']:
                    rec(arg)
        elif isinstance(f, list):
            for i in f:
                rec(i)
    rec(from_clause)
    return join_count, on_missing

def _find_window_functions(node):
    if isinstance(node, dict):
        if 'over' in node:
            return True
        return any(_find_window_functions(v) for v in node.values())
    if isinstance(node, list):
        return any(_find_window_functions(i) for i in node)
    return False

def _extract_predicates_where(where_ast):
    cols = set()
    if isinstance(where_ast, dict):
        for k, v in where_ast.items():
            if k in ('and', 'or'):
                conds = v if isinstance(v, list) else [v]
                for cond in conds:
                    cols.update(_extract_predicates_where(cond))
            elif k in ('eq', 'gt', 'lt', 'gte', 'lte', 'neq'):
                if isinstance(v, list) and len(v) == 2:
                    left, right = v
                    if isinstance(left, dict) and 'literal' in left:
                        cols.add(left['literal'])
                    elif isinstance(left, str):
                        cols.add(left)
                    if isinstance(right, dict) and 'literal' in right:
                        cols.add(right['literal'])
                    elif isinstance(right, str):
                        cols.add(right)
            else:
                cols.update(_extract_predicates_where(v))
    return cols

def _has_partition_filter(where_cols):
    partition_keywords = ['_PARTITIONTIME', '_PARTITIONDATE', 'DATE', 'TIMESTAMP', 'PARTITION_']
    return any(any(pk in col.upper() for pk in partition_keywords) for col in where_cols)

def _has_aggregation(node):
    agg_funcs = {'count', 'sum', 'avg', 'min', 'max'}
    if isinstance(node, dict):
        for k, v in node.items():
            if k.lower() in agg_funcs:
                return True
            if _has_aggregation(v):
                return True
    if isinstance(node, list):
        return any(_has_aggregation(i) for i in node)
    return False

def _extract_join_conditions(from_ast):
    join_keys = set()
    def rec(node):
        if isinstance(node, dict):
            if 'join' in node and 'on' in node:
                on_cond = node['on']
                join_keys.update(_extract_predicates_where(on_cond))
                rec(node['join'])
            if 'value' in node:
                rec(node['value'])
            if 'args' in node and isinstance(node['args'], list):
                for arg in node['args']:
                    rec(arg)
        elif isinstance(node, list):
            for n in node:
                rec(n)
    rec(from_ast)
    return join_keys

def estimate_carbon_emission(bytes_processed):
    gb_processed = bytes_processed / (1024 ** 3)
    return gb_processed * 0.0005

def generate_record_hash(file_path, recommendations):
    combined = file_path + "||" + "|".join(sorted(recommendations))
    return hashlib.sha256(combined.encode('utf-8')).hexdigest()
