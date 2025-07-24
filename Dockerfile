FROM python:3.9.6-slim

WORKDIR /app

COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

COPY src/ ./src/
COPY sql/ ./sql/
COPY alerts/ ./alerts/
COPY ddl/ ./ddl/
COPY infra/ ./infra/
COPY ml_models/ ./ml_models/
COPY tests/ ./tests/


ENV PYTHONPATH="/app/src"

EXPOSE 8080


CMD ["python3", "-m", "src.main"]