FROM python:3.11-slim

WORKDIR /app

# Instala dependências 
RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copia aplicação
COPY . /app

EXPOSE 8501

ENV PYTHONUNBUFFERED=1

# Comando padrão: roda o dashboard Streamlit
CMD ["streamlit", "run", "Dashboard_graphics.py", "--server.port", "8501", "--server.address", "0.0.0.0"]
