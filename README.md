# Spring Boot LINE AI Manager Bot

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue.svg)](https://maven.apache.org/)
[![LINE API](https://img.shields.io/badge/LINE%20API-Messaging-green.svg)](https://developers.line.biz/)
[![Python](https://img.shields.io/badge/Python-3.10+-blue.svg)](https://www.python.org)

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
  - 注意事項：版本號需唯一（不可有兩個相同 `V2__...`），已套用的 migration 不要直接修改，要新增新的版本檔（例如 `V4__...sql`）。若需要可重覆執行的 migration，使用 repeatable migration（`R__description.sql`）。如需接受先前順序之外的版本，可設定 `spring.flyway.out-of-order=true`（慎用）。
  - 驗證：啟動後檢查資料表 `flyway_schema_history` 或查看啟動日誌確認哪些 migration 已套用。

---

## 主要功能
- 透過 Webhook 接收使用者發送的文字訊息。
- Echo 功能：原封不動回覆訊息。
- 支援 LINE Bot SDK for Java，簽章驗證。
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
   - 請參考 `.env.example`，建立 `.env.dev` 並填入金鑰與 DB 連線資訊。
4. **啟動 Spring Boot 應用程式**
   - 一鍵啟動：
     ```bash
     ./start-dev.sh
     ```
   - 或手動：
     ```bash
     export $(grep -v '^#' .env.dev | xargs) && mvn spring-boot:run
     ```
5. **使用 ngrok 建立公開通道**
   ```bash
   ngrok http 8080
   ```
   - 複製 ngrok HTTPS 網址，於 LINE Developers Console 設定 Webhook URL（加上 `/webhook`）。

---

## 資料庫設定
- PostgreSQL 連線：
  ```bash
  psql -h localhost -p 5432 -U postgres -W
  ```
- pom.xml 依賴：
  ```xml
  <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.7.3</version>
  </dependency>
  ```
- application.properties 範例：
  ```properties
  spring.datasource.url=${SPRING_DATASOURCE_URL}
  spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
  spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
  spring.datasource.driver-class-name=org.postgresql.Driver
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true
  logging.level.org.hibernate.SQL=DEBUG
  logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
  ```
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

## 本機啟動
- 一鍵啟動：
  ```bash
  ./start-dev.sh
  ```
- 或手動：
  ```bash
  export $(grep -v '^#' .env.dev | xargs) && mvn spring-boot:run
  ```

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
├── main.py           # FastAPI 主程式
├── models.py         # 請求/回應資料結構
├── service.py        # 各領域分析邏輯
├── prompts.py        # AI 提示詞
├── requirements.txt  # Python 依賴
├── Dockerfile        # 容器化設定
└── README.md         # NLPService 說明
```

### 啟動方式
1. 安裝 Python 依賴：
   ```bash
   cd NLPService
   pip install -r requirements.txt
   ```
2. 啟動 FastAPI 服務：
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
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

## 授權 (License)
本專案採用 MIT 授權。詳情請見 `LICENSE` 檔案。