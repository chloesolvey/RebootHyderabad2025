import re

def read_sql_file(file_path):
    """
    Read the entire content of a SQL file at the given file path.
    
    Args:
        file_path (str): Path to the SQL file
        
    Returns:
        str: Content of the file as a string
    """
    with open(file_path, 'r') as f:
        return f.read()

def split_sql_statements(sql_text):
    """
    Splits a block of SQL text into individual statements using semicolon ';' as delimiter.
    Note: This simple implementation may fail if semicolons exist inside strings or comments.
    
    Args:
        sql_text (str): Multi-statement SQL as a string
        
    Returns:
        list: List of individual SQL statements as strings
    """
    return [stmt.strip() for stmt in sql_text.split(';') if stmt.strip()]

def _detect_main_keyword_after_cte(sql):
    """
    Heuristic to detect the main SQL command keyword following a CTE declaration block.
    Counts parentheses to find where CTEs end and returns the next statement's first keyword.
    
    Args:
        sql (str): SQL statement starting with WITH for CTE(s)
        
    Returns:
        str: Main SQL command keyword, e.g., SELECT, INSERT, CREATE, etc.
    """
    parens = 0
    for i, char in enumerate(sql):
        if char == '(':
            parens += 1
        elif char == ')':
            parens -= 1
            if parens == 0:
                rest = sql[i+1:].lstrip()
                match = re.match(r'^(\w+)', rest, re.IGNORECASE)
                return match.group(1).upper() if match else 'SELECT'
    return 'SELECT'

def classify_sql_statements(sql_text):
    """
    Classify SQL statements from the input text into SELECT, INSERT, CREATE, or OTHER categories.  
    Properly handles queries starting with CTE (WITH clause) to detect underlying statements.
    
    Args:
        sql_text (str): Multi-statement SQL to classify
        
    Returns:
        dict: Dictionary with keys 'select', 'insert', 'create', 'other' containing lists of statements
    """
    statements = split_sql_statements(sql_text)
    select_statements = []
    insert_statements = []
    create_statements = []
    other_statements = []

    for stmt in statements:
        stripped_stmt = stmt.strip()
        first_word_match = re.match(r'^\s*(?:--.*\n\s*)*(\w+)', stripped_stmt, re.IGNORECASE)
        if not first_word_match:
            other_statements.append(stmt)
            continue

        first_word = first_word_match.group(1).upper()

        if first_word == 'WITH':
            main_keyword = _detect_main_keyword_after_cte(stripped_stmt)
            if main_keyword == 'SELECT':
                select_statements.append(stmt)
            elif main_keyword == 'INSERT':
                insert_statements.append(stmt)
            elif main_keyword == 'CREATE':
                create_statements.append(stmt)
            else:
                other_statements.append(stmt)
        elif first_word == 'SELECT':
            select_statements.append(stmt)
        elif first_word == 'INSERT':
            insert_statements.append(stmt)
        elif first_word == 'CREATE':
            create_statements.append(stmt)
        else:
            other_statements.append(stmt)

    return {
        'select': select_statements,
        'insert': insert_statements,
        'create': create_statements,
        'other': other_statements
    }
