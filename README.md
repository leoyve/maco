# Spring Boot LINE AI Manager Bot

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com)
[![Python](https://img.shields.io/badge/Python-3.10+-blue.svg)](https://www.python.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue.svg)](https://maven.apache.org/)
[![LINE API](https://img.shields.io/badge/LINE%20API-Messaging-green.svg)](https://developers.line.biz/)
[![Postgres](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org)
[![Flyway](https://img.shields.io/badge/Flyway-11-orange.svg)](https://flywaydb.org)
[![Docker](https://img.shields.io/badge/Docker-Container-blue.svg)](https://www.docker.com)
[![GCP](https://img.shields.io/badge/GCP-Google%20Cloud-blue.svg)](https://cloud.google.com)
[![Cloud Run](https://img.shields.io/badge/Cloud%20Run-deployed-green.svg)](https://cloud.google.com/run)
[![Cloud SQL](https://img.shields.io/badge/Cloud%20SQL-Postgres-blue.svg)](https://cloud.google.com/sql)
[![CI](https://github.com/leoyve/maco/actions/workflows/deploy-java-backend.yml/badge.svg)](https://github.com/leoyve/maco/actions)
[![CI](https://github.com/leoyve/maco/actions/workflows/deploy-nlp-service.yml/badge.svg)](https://github.com/leoyve/maco/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

---

## 功能概覽

- LINE Bot Webhook：接收並回覆使用者訊息，支援簽章驗證（LINE Messaging API）。
- 代辦事項（TODO）管理：支援新增、查詢、完成、刪除，使用 NLPService 解析使用者語意以判別動作與欄位。
- Flex 訊息支持：可從 classpath 範本或動態產生 Flex JSON（LineFlexMessageBuilder），並能容錯包裝 bubble/contents 片段後發送。
- 時間處理集中化：DateTimeUtils 提供統一的時間解析/格式化（支援 yyyy-MM-dd HH:mm 與 ISO-8601），供 Mapper/Service 共用。
- 時間範圍查詢：JPA derived query 支援依狀態與時間區間查詢（Repository + Jpa adapter 已實作）。
- 資料持久化：PostgreSQL + Spring Data JPA，Entity 支援 JPA Auditing（createdAt/updatedAt）。
- 非同步與高併發設計：使用 @Async + CompletableFuture，NLP 與 domain 處理可非同步執行以提升吞吐量。
- NLPService（外部 Python FastAPI）：提供 /router 與 /process 供語意分類與欄位解析，Java 端透過 HTTP 整合。
- 錯誤處理與日誌：SLF4J 日誌、Flex 反序列化容錯（失敗時回退為文字回覆）、以及多處日誌紀錄以利除錯。
- 開發/部署工具：包含 start-dev.sh、ngrok 支援與可選的 Docker 化 NLPService。

## 注意事項：按鈕互動、Flex 與埠設定
- 按鈕交互限制：目前 UI 上的按鈕支援「完成」與「刪除」，暫時不支援直接在卡片內修改代辦（避免修改流程與欄位衝突）。若使用者要修改事項，請先點選刪除再重新新增。
- Postback data 欄位：Line 的按鈕會以 postback data 傳回參數，本專案使用的約定為 `action`（動作）與 `todo_id`（代辦資料唯一 id），範例： `action=complete_todo&todo_id=42`。
- Flex 範本與動態產生：Flex 範本檔案位於 `src/main/resources/flex/todo_list_template.json`，實作中以 `LineFlexMessageBuilder` 將 `List<TodoResult>` 動態注入範本內容產生最終 JSON，再交由 `LineService` 發送。若要調整樣式，請編輯範本檔或調整 `LineFlexMessageBuilder#createTodoItemBox` 的輸出。
- Flex JSON 傳輸容錯：LineService 可接收完整的 `type:flex` 結構或僅包含 `contents`（如 single bubble）的片段；系統會自動將片段包成完整的 `FlexMessage` 後發送，若解析失敗會回退為文字回覆。
- 變更應用埠：若預設 8080 被佔用，可臨時以啟動參數改埠（`--server.port=8081`），或在 `src/main/resources/application.properties` 新增 `server.port=8081` 做為預設。

---

## 目錄
- [更新紀錄](#更新紀錄)
- [主要功能](#主要功能)
- [技術棧](#技術棧)
- [開始使用](#開始使用)
- [資料庫設定](#資料庫設定)
- [本機啟動](#本機啟動)
- [NLPService (AI 語意分析模組)](#nlpservice-ai-語意分析模組)
- [授權](#授權)

---

## 更新紀錄
2025/08/17
- 新增 LINE Bot 訊息儲存至 PostgreSQL DB，並設計 Entity、Repository、Mapper、DTO 分層。
- Controller 層改用 DTO 封裝訊息資料。
- Service 層支援訊息儲存、查詢、回覆。
- 新增 pushMessage 方法，可主動推播訊息給使用者。
- 導入 SLF4J Logger，所有錯誤訊息改用 log.error 記錄。
- pom.xml 增加 HikariCP、Spring Data JPA、PostgreSQL JDBC driver 依賴。
- application.properties 增加 HikariCP 連線池與 SQL 日誌設定。
- README.md 增加 DB 設計、Spring Boot 連接 PostgreSQL 教學。
- 新增 Flyway 版本化 migration 支援：將 SQL migration 檔放在 `src/main/resources/db/migration/`（例如 `V1__init_schema.sql`、`V2__...sql`、`V3__...sql`）。已在 `pom.xml` 加入 `flyway-core` 與 `flyway-database-postgresql` 依賴，啟動時 Spring Boot 會自動套用未執行的版本。
  - 注意事項：版本號需唯一（不可有兩個相同 `V2__...`）。
  - 驗證：啟動後檢查資料表 `flyway_schema_history` 或查看啟動日誌確認哪些 migration 已套用。

2025/09/08
- 修正 `LineFlexMessageBuilder`
  - 加入 defensive null-check，避免 `todo.getEntities().getTime()` 為 null 導致 NPE。
  - 保證所有 Flex text 欄位為非空字串，避免 LINE API 回傳 "must be non-empty text"。
  - time/location 欄位支援自動換行（`wrap: true`, `maxLines: 2`），若有地點會另起一行顯示 "@ 地點"；無地點則不顯示該行。
- `NLPService`
  - 更新 `prompts.py` 中 `get_todo_prompt`：強制 JSON-only 回傳，time 使用 `yyyy-MM-dd HH:mm`；範例加入單一時間與時間範圍格式。
- `LineService` / Messaging
  - 補充：replyMessage 一次最多可回 5 則訊息（Flex、Text 混合計算）。
- JPA 刪除查詢（注意）
  - 若用 JPQL 的 DELETE 查詢，需於 repository 方法加上 `@Modifying` 與 `@Transactional`，或改用 `EntityManager.createQuery(...).executeUpdate()`，否則會拋出 `IllegalSelectQueryException`。
- UI 色彩建議
  - 進行中（in-progress）推薦顏色：`#FFB74D`（橙色，與完成綠 `#1DB446` 對比明顯）。
  - 亮綠候選（供選擇）：`#00E676`、`#00C853`、`#1DB446`。
- 測試與驗證建議
  - 針對 `time=null`、`location=null` 與 `task` 為空的情境做單元/整合測試；在本地先印出 Flex JSON 確認所有 text 欄位非空再發送。

2025/09/18
- 新增 DB migration
  - 新增 `V5__add_finish_time_to_todos.sql`（ALTER TABLE 新增 `finish_time` 欄位，type = `timestamptz`，使用 `IF NOT EXISTS` 保護重複執行）。
- JPA / Repository
  - `TodoJpaRepo` 查詢改為：`ORDER BY CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END ASC, t.todoTime ASC`，將已完成項目排到最後。
  - 刪除查詢已改為使用 `@Modifying` + `@Transactional` 或 `executeUpdate()`（避免 `IllegalSelectQueryException`）。
- Flex 與 UI
  - `LineFlexMessageBuilder`：加入 defensive null-checks，確保所有 Flex `text` 欄位非空，並讓 time/location 支援換行（`wrap: true` 與 `maxLines`）；若有地點，會在新行顯示 `@ 地點`。
  - 建議將 LINE 圖片資源放到 `src/main/resources/static/line-assets/`（開發時可搭配 `ngrok` 暴露），或在生產環境使用 S3/CDN 提供 HTTPS URL。
- LineService
  - 在 `sendFlexReplyFromJson` 中改為重用 `ObjectMapper`（避免每次 new），並加入解析與建構的 timing log 以便量化耗時。
  - 建議將 messaging API 的 network call 改為非同步或避免在主流程直接 `.get()`（以免阻塞）。
- NLPService
  - `prompts.py` 中 `get_todo_prompt` 已更新，強制 JSON-only 回傳，time 格式為 `yyyy-MM-dd HH:mm`，並提供多個範例。

---
## 待辦
- 可以考慮查詢作非同步，改用Push
- FlexMessage 分頁功能
- 使用者一次輸入，一次幫我新增完待辦
 ```text
9/21 慶生
9/26 拿蛋黃酥
10/1 島語
 ```

---
## 主要功能
- 透過 Webhook 接收使用者發送的文字訊息。
- 訊息儲存至 PostgreSQL。
- 查詢歷史訊息。
- 主動推播訊息（push message）。
- 分層設計：Controller / Service / Repository / Entity / DTO / Mapper。
- 日誌管理（SLF4J Logger）。

---

## 技術棧
- Spring Boot
- Java 17+
- Maven
- LINE Messaging API
- line-bot-sdk-java (v5.1.0)
- PostgreSQL
- Spring Data JPA
- HikariCP
- ngrok

---

## 開始使用
### 前置需求
- Java Development Kit (JDK) 17+
- Apache Maven
- ngrok
- LINE Developers 帳號

### 安裝與設定
1. **Clone 專案**
   ```bash
   git clone [https://github.com/](https://github.com/)[您的GitHub用戶名]/[您的專案名稱].git
   cd [您的專案名稱]
   ```
2. **設定 LINE Channel**
   - 取得 Channel secret 與 Channel access token。
3. **設定環境變數**
4. **使用 ngrok 建立公開通道**
   ```bash
   ngrok http 8080
   ```
   - 複製 ngrok HTTPS 網址，於 LINE Developers Console 設定 Webhook URL（加上 `/webhook`）。

---

## 資料庫設定
- Flyway migration：
  - 將 SQL migration 檔放在 `src/main/resources/db/migration/`（例如 `V1__init_schema.sql`、`V2__...sql`、`V3__...sql`）。
  - 已在 `pom.xml` 加入 `flyway-core` 與 `flyway-database-postgresql` 依賴，啟動時 Spring Boot 會自動套用未執行的版本。
  - 注意事項：
    - 版本號需唯一（不可有兩個相同 `V2__...`）。
    - 已套用的 migration 不要直接修改，要新增新的版本檔（例如 `V4__...sql`）。
    - 若需要可重覆執行的 migration，使用 repeatable migration（`R__description.sql`）。
    - 如需接受先前順序之外的版本，可設定 `spring.flyway.out-of-order=true`（慎用）。
  - 驗證：啟動後檢查資料表 `flyway_schema_history` 或查看啟動日誌確認哪些 migration 已套用。
- 建議使用 Spring Data JPA 存取資料。
---

## 高併發非同步設計

本專案 LINE Bot 訊息處理採用 Spring Boot @Async + CompletableFuture 非同步設計：
- 每則訊息獨立執行，主流程不阻塞。
- NLPService（/router, /process）API 呼叫皆在獨立執行緒完成。
- 各 domain（如代辦事項、健康紀錄）邏輯也獨立非同步執行。
- 可同時處理大量訊息，效能取決於主機資源與 ThreadPool 設定。

範例流程：
1. handleTextMessage() 以 @Async 非同步執行，訊息存入 DB 後，非同步呼叫 NLP /router 判斷 domain。
2. 根據 domain 再分流至對應 Service（如 handleTodoAsync），同樣以 CompletableFuture 非同步呼叫 NLP /process。
3. 各 Service 可在分析完成後主動回覆訊息或進行後續處理。

> 若主機資源充足，可用 Spring 預設執行緒池；高併發場景建議自訂 ThreadPoolTaskExecutor 參數。

---

## NLPService (AI 語意分析模組)

詳細說明請見 [NLPService/README.md](NLPService/README.md)

本專案包含獨立的 Python NLPService，負責多領域語意分類與分析：

- 採用 FastAPI 實作，支援高效 REST API。
- 主要 API：
  - `/router`：分類使用者文字，判斷主 domain（如 todo、health）。
  - `/process`：根據 domain 進行細部語意分析。
- 支援多領域（如代辦事項、健康紀錄），可彈性擴充。
- 介接方式：Java 端以 HTTP 呼叫 `/router`、`/process`，取得 NLP 結果。
- 具備健康檢查 API `/`，方便監控服務狀態。

### NLPService 目錄結構
```
NLPService/
├── main.py           # FastAPI Service
├── models.py         # Request/Response Data Structures
├── service.py        # 各領域分析邏輯
├── prompts.py        # AI Prompt
├── requirements.txt  # Python Dependence
├── Dockerfile        # 容器化設定
└── README.md         # NLPService 說明
```

### 啟動方式
1. 安裝 Python 依賴：
   ```bash
   cd NLPService
   pip install -r requirements.txt
   ```
2. 啟動 FastAPI 服務（開發模式）：
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```
3. 建置為 Docker 映像（若要在容器中執行）：
   ```bash
   # 在 NLPService 目錄下執行（範例：在 macOS/Apple Silicon 架構上強制以 amd64 平台建置）
   docker build --platform linux/amd64 -t python-nlp-service .
   # 執行容器（將 8000 port 對應到主機）
   docker run --rm -p 8000:8000 python-nlp-service
   ```

---

## 代辦事項
- 監控 NlpClient 重試次數與失敗率（建議：加入 metrics，觀察是否需要調整 retry 策略）。
- 視流量需求評估是否導入 Resilience4j（retry + circuit breaker + bulkhead）。
- 若使用非同步/ThreadPool，確認 MDC / traceId 傳遞（以便日誌追蹤）。


---

## 專案結構
```
Maestro/
├── pom.xml
├── README.md
├── start-dev.sh
├── image/
├── NLPService/
│   ├── Dockerfile
│   ├── main.py
│   ├── models.py
│   ├── prompts.py
│   ├── requirements.txt
│   ├── service.py
│   └── README.md
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── maco/
│       │               ├── LineEchoBotApplication.java
│       │               ├── linebot/
│       │               │   ├── LineBotController.java
│       │               │   ├── LineService.java
│       │               │   ├── model/
│       │               │   │   ├── ProcessRequest.java
│       │               │   │   ├── ProcessResponse.java
│       │               │   │   ├── RouterRequest.java
│       │               │   │   └── RouterResponse.java
│       │               │   └── util/
│       │               │       └── NlpClient.java
│       │               ├── domain/
│       │               │   ├── dto/
│       │               │   │   ├── BaseResultDto.java
│       │               │   │   ├── LineMessageDto.java
│       │               │   │   └── TodoResultDto.java
│       │               │   ├── model/
│       │               │   │   ├── BaseResult.java
│       │               │   │   ├── todo/
│       │               │   │   │   └── TodoResult.java
│       │               │   │   └── user/
│       │               │   └── port/
│       │               │       └── user/
│       │               ├── infra/
│       │               │   ├── jpa/
│       │               │   │   ├── adapter/
│       │               │   │   │   └── JpaTodoRepository.java
│       │               │   │   ├── entity/
│       │               │   │   │   ├── TodoEntity.java
│       │               │   │   │   ├── UserEntity.java
│       │               │   │   │   └── LineMessageEntity.java
│       │               │   │   ├── mapper/
│       │               │   │   │   └── TodoMapper.java
│       │               │   │   └── repo/
│       │               │   │       ├── TodoJpaRepo.java
│       │               │   │       └── UserJpaRepo.java
│       │               ├── todo/
│       │               │   └── TodoService.java
│       │               └── user/
│       │                   ├── LineMessageService.java
│       │                   └── UserService.java
│       └── resources/
│           └── application.properties
└── target/
    └── ...
```

---