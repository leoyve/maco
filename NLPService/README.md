# NLPService - FastAPI x Gemini 代辦事項語意分析 API

## 服務簡介
本專案提供 RESTful API，結合 Google Gemini AI，能將自然語言輸入（中文）解析為結構化 JSON，支援：
- 新增、查詢、修改代辦事項
- 時間、地點、事項、狀態抽取
- 信心分數判斷，若語意不明確自動回傳建議

## 主要功能
- `/analyze`：POST，輸入 text，回傳 Gemini 解析後的 JSON（含 intent、task、time、location、status、is_clear、recommendation 等欄位）
- 自動推算「明天」、「本週」、「本月」等時間語意
- 若信心分數低於 0.7，回傳 `recommendation` 欄位，建議使用者如何輸入

## 使用方式
1. 安裝依賴
   ```sh
   pip install -r requirements.txt
   ```
2. 設定 `.env` 檔案，填入 `GEMINI_API_KEY`
3. 啟動服務
   ```sh
   source venv/bin/activate && uvicorn main:app --reload
   ```
4. 測試 API
   - 使用 Swagger UI: http://127.0.0.1:8000/docs
   - 或用 curl：
     ```sh
     curl -X POST "http://127.0.0.1:8000/analyze" -H "Content-Type: application/json" -d '{"text": "明天下午三點在台北開會"}'
     ```

## Docker 部署建議
- 可用 Dockerfile 打包，確保依賴與環境一致，方便雲端部署。

## Docker 部署
1. 建立映像檔
   ```sh
   docker build -t nlp-service .
   ```
2. 啟動容器並注入金鑰（建議用 .env 檔案）
   ```sh
   docker run -d -p 8000:8000 --env-file .env nlp-service
   ```
3. 服務啟動後，可用瀏覽器或 curl 測試 API

> 若用 Docker Desktop GUI，請在「環境」設定區手動填入 GEMINI_API_KEY

## 範例回傳
```json
{
  "intent": "addTodo",
  "task": "跟 David 開會",
  "time": { "timestamp": "2025-08-19 15:00" },
  "location": "台北",
  "status": "TODO",
  "is_clear": true,
  "recommendation": null
}
```

## API JSON 格式說明
- 回傳欄位依據語意分析結果，主要包含：
  - intent：意圖（如 addTodo, queryTodo, modifyTodoStatus）
  - entities：結構化抽取（如 task, time, location, status）
  - is_clear：語意是否明確
  - recommendation：若語意不明，回饋建議
- 範例：
```json
{
  "intent": "addTodo",
  "entities": {
    "task": "跟 David 開會",
    "time": { "timestamp": "2025-08-19 15:00" }
  },
  "is_clear": true,
  "recommendation": null
}
```

## 多模組路由擴充
- NLP 路由已模組化（routers/nlp_router.py），主 app 掛載。
- 未來可新增 user、todo 等模組，於 routers/ 目錄建立 router 並在 main.py 掛載。

## 進階部署
- 可自訂 .dockerignore 排除 venv、__pycache__ 等無需打包檔案。
- 若需多服務協作，可撰寫 docker-compose.yml 管理多容器。

## 測試與前端串接
- 可用 Swagger UI 互動測試 API。
- 前端建議直接解析回傳 JSON，根據 intent/信心分數顯示建議。

## 聯絡/貢獻
歡迎 issue、PR 或討論！

## 目錄結構
```
NLPService/
├── main.py
├── venv/
└── README.md
```
