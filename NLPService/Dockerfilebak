# 改進版 Dockerfile for NLPService (FastAPI)
FROM python:3.10-slim

# 建議的環境變數
ENV PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

WORKDIR /app

# 安裝系統相依（包含 curl 與編譯套件，如果 requirements 需要編譯原生套件）
RUN apt-get update \
    && apt-get install -y --no-install-recommends gcc libpq-dev curl build-essential \
    && rm -rf /var/lib/apt/lists/*

# 先複製 requirements 以利用 Docker layer cache
COPY requirements.txt ./

# 安裝 Python 相依
RUN pip install --upgrade pip \
    && pip install -r requirements.txt

# 建立非 root 使用者並複製原始碼
RUN useradd -m appuser
COPY . /app
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8000

# 健康檢查（需要 curl 已安裝）
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8000/ || exit 1

# 預設啟動 FastAPI（可以在 docker run 時改 CMD 或加入參數）
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--proxy-headers"]
