# 基礎映像檔
FROM python:3.9-slim

# 設定工作目錄
WORKDIR /app

# 複製專案檔案
COPY . /app

# 安裝依賴
RUN pip install --upgrade pip \
    && pip install -r requirements.txt

# 設定環境變數（可用 .env 或外部注入）
ENV PYTHONUNBUFFERED=1

# 暴露 8000 port
EXPOSE 8000

# 預設啟動 FastAPI 服務
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
