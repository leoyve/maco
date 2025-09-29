# Spring Boot LINE AI Manager Bot

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com)
[![Python](https://img.shields.io/badge/Python-3.10+-blue.svg)](https://www.python.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue.svg)](https://maven.apache.org/)
[![LINE API](https://img.shields.io/badge/LINE%20API-Messaging-green.svg)](https://developers.line.biz/)
[![Postgres](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org)
[![Flyway](https://img.shields.io/badge/Flyway-11-orange.svg)](https://flywaydb.org)
[![Docker](https://img.shields.io/badge/Docker-Container-blue.svg)](https://www.docker.com)
[![Cloud Run](https://img.shields.io/badge/GCP-Cloud%20Run-green.svg)](https://cloud.google.com/run)
[![Cloud SQL](https://img.shields.io/badge/GCP-Cloud%20SQL-blue.svg)](https://cloud.google.com/sql)
[![CI: Backend](https://github.com/leoyve/maco/actions/workflows/deploy-java-backend.yml/badge.svg)](https://github.com/leoyve/maco/actions)
[![CI: NLP](https://github.com/leoyve/maco/actions/workflows/deploy-nlp-service.yml/badge.svg)](https://github.com/leoyve/maco/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

> 用 Java + Spring Boot 實作的 LINE AI 管理助理 Bot，搭配獨立的 Python FastAPI NLPService 進行語意解析。已導入 PostgreSQL + Flyway 進行版本化資料庫管理，並支援 Cloud Run / Cloud SQL 部署與 GitHub Actions CI/CD。

---

## 目錄
- [功能總覽](#功能總覽)
- [架構與設計](#架構與設計)
- [技術棧](#技術棧)
- [快速開始](#快速開始)
- [環境變數與設定](#環境變數與設定)
- [資料庫與 Migration](#資料庫與-migration)
- [NLPService (Python)](#nlpservice-python)
- [部署到 GCP (Cloud Run + Cloud SQL)](#部署到-gcp-cloud-run--cloud-sql)
- [CI/CD](#cicd)
- [非同步與高併發](#非同步與高併發)
- [日誌與觀測性](#日誌與觀測性)
- [安全性](#安全性)
- [待辦與 Roadmap](#待辦與-roadmap)
- [專案結構](#專案結構)
- [授權](#授權)

---

## 功能總覽
- **LINE Webhook**：接收並回覆訊息，支援簽章驗證。
- **代辦事項 (TODO)**：新增 / 查詢 / 完成 / 刪除，透過 NLP 解析指令與欄位。
- **Flex Message**：支援從範本或動態生成 Flex JSON，具備容錯包裝與回退策略。
- **時間處理集中化**：`DateTimeUtils` 統一解析與格式化（`yyyy-MM-dd HH:mm`、ISO-8601）。
- **時間範圍查詢**：JPA 派生查詢，支援狀態 + 區間過濾。
- **資料持久化**：PostgreSQL + Spring Data JPA，支援 JPA Auditing (`createdAt/updatedAt`)。
- **非同步處理**：`@Async` + `CompletableFuture`，NLP 與 domain handler 分流執行。
- **錯誤處理**：SLF4J 統一日誌；Flex 解析失敗回退文字；多點關鍵 log。
- **主動推播**：`pushMessage` 可主動通知使用者。
> **互動限制**：目前卡片按鈕僅支援「完成」與「刪除」，不支援卡片內即時修改，以避免流程衝突。需要修改請刪除後重建。

---

## 架構與設計

```
+------------------+        HTTP (Webhook)        +---------------------+
|     LINE App     |  ─────────────────────────▶  |  Spring Boot App    |
| (User Messages)  |                               |  (Webhook + Domain) |
+------------------+                               |  - LineService      |
                                                   |  - TodoService      |
                                                   |  - NlpClient        |
                                                   +----------┬----------+
                                                              │
                                   HTTP (/router, /process)   │ Async
                                                              │
                                                   +----------▼----------+
                                                   |   NLPService (Py)   |
                                                   |   FastAPI + LLM     |
                                                   +----------┬----------+
                                                              │
                                                   JDBC       │ JPA
                                                              │
                                                   +----------▼----------+
                                                   |   PostgreSQL 16     |
                                                   |  (Cloud SQL / Local)|
                                                   +---------------------+
```

- **Domain 分層**：Controller / Service / Repository / Entity / DTO / Mapper。
- **FlexBuilder**：`LineFlexMessageBuilder` 將 `List<TodoResult>` 注入 JSON 模板，並保證 `text` 欄位非空、`wrap/maxLines` 設定妥當。
- **排序策略**：查詢時將已完成項目置後，進行中按 `todoTime` 排序。
- **NLP 介接**：`NlpClient` 呼叫 `/router` 進行 domain 判斷，再依 domain 呼叫 `/process` 解析欄位。

---

## 技術棧
- **後端**：Java 17、Spring Boot 3.3.x、Maven、line-bot-sdk-java 5.1.0
- **資料庫**：PostgreSQL 16、Spring Data JPA、HikariCP、Flyway 11
- **AI / NLP**：Python 3.10+、FastAPI、(LLM by prompt)
- **Infra**：Docker、GCP Cloud Run、Cloud SQL、ngrok（本機開發）
- **CI/CD**：GitHub Actions（Java Backend / NLPService）

---

## 快速開始

### 前置需求
- JDK 17+
- Python 3.10+
- Maven 3.9+
- Docker（選用）
- LINE Developers 帳號 & Channel（Messaging API）
- ngrok（本機 Webhook 測試）

### 1) 取得原始碼
```bash
git clone https://github.com/leoyve/maco.git
cd maco
```

### 2) 設定環境變數
請參考下方[環境變數與設定](#環境變數與設定)。

### 3) 啟動 NLPService (本機)
```bash
cd NLPService
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

（或使用 Docker）
```bash
cd NLPService
# Apple Silicon 亦可指定平台
docker build --platform linux/amd64 -t python-nlp-service .
docker run --rm -p 8000:8000 python-nlp-service
```

### 4) 啟動 Spring Boot
```bash
# 預設 8080，可透過 --server.port 覆寫
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
```

### 5) 啟動 ngrok 並設定 LINE Webhook
```bash
ngrok http 8080
```
將 ngrok 產生的 **HTTPS** URL + `/webhook` 設為 LINE Channel 的 Webhook URL。

---

## 環境變數與設定

> 建議以 `.env` / Secret Manager / Cloud Run 環境變數管理，不要將敏感資訊提交版本庫。

| 變數 | 說明 | 範例 |
|---|---|---|
| `LINE_CHANNEL_SECRET` | LINE Messaging API Channel Secret | `xxxxxxxx...` |
| `LINE_CHANNEL_TOKEN`  | LINE Messaging API Channel Access Token | `xxxxxxxx...` |
| `SPRING_DATASOURCE_URL` | PostgreSQL 連線字串 | `jdbc:postgresql://localhost:5432/maco` |
| `SPRING_DATASOURCE_USERNAME` | DB 使用者 | `maco` |
| `SPRING_DATASOURCE_PASSWORD` | DB 密碼 | `******` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | schema 管理 | `validate`（配合 Flyway） |
| `SPRING_FLYWAY_ENABLED` | 啟用 Flyway | `true` |
| `NLP_BASE_URL` | NLPService 基底 URL | `http://localhost:8000` 或雲端位址 |
| `TZ` | 時區 | `Asia/Taipei` |

> **HikariCP**、SQL log、ThreadPool 等高階設定可置於 `application.properties` / `application.yml`。

---

## 資料庫與 Migration
- 使用 Flyway 進行版本化 migration：
  - 檔名置於 `src/main/resources/db/migration/`，如 `V1__init_schema.sql`、`V5__add_finish_time_to_todos.sql`。
  - 已套用之版本不得修改，需以新版本遞增（`V2__...` → `V3__...`）。
  - 可用 `R__*.sql` 做 repeatable migration。
  - 需要容忍版本順序時，方可設定 `spring.flyway.out-of-order=true`（謹慎使用）。
- 啟動後可檢查 `flyway_schema_history` 以及啟動日誌以驗證。
- **近期變更**：新增 `finish_time TIMESTAMPTZ` 欄位（`V5__add_finish_time_to_todos.sql`），並以 `IF NOT EXISTS` 防止重複執行。

---

## NLPService (Python)

**主要 API**
- `GET /`：健康檢查
- `POST /router`：分類使用者文字，回傳主 domain（e.g., `todo`, `health`）
- `POST /process`：依 domain 解析欄位（e.g., `task`, `time`, `location`, `status`）

**目錄結構**
```
NLPService/
├── main.py           # FastAPI 入口
├── models.py         # 請求/回應資料結構
├── service.py        # 各領域解析邏輯
├── prompts.py        # LLM Prompt 與格式規範（JSON-only）
├── requirements.txt  # 套件相依
├── Dockerfile        # 容器化設定
└── README.md
```

**時間格式**：`yyyy-MM-dd HH:mm`；回傳為 JSON-only，避免解析歧義。

---

## 部署到 GCP (Cloud Run + Cloud SQL)

### 前置
- 已建立 GCP 專案、啟用 Cloud Run / Cloud SQL API。
- 建立 Postgres 16 的 Cloud SQL instance 與資料庫。
- 使用 Secret Manager 管理敏感資訊（建議）。

### 建置容器映像
```bash
# 後端
gcloud builds submit --tag gcr.io/$PROJECT_ID/maco-backend:$(git rev-parse --short HEAD)
# NLP
gcloud builds submit NLPService --tag gcr.io/$PROJECT_ID/maco-nlp:$(git rev-parse --short HEAD)
```

### 部署 Cloud Run（示例）
```bash
gcloud run deploy maco-backend \
  --image gcr.io/$PROJECT_ID/maco-backend:$(git rev-parse --short HEAD) \
  --region asia-east1 \
  --allow-unauthenticated \
  --set-env-vars LINE_CHANNEL_SECRET=...,LINE_CHANNEL_TOKEN=...,NLP_BASE_URL=https://maco-nlp-xxxxx-a.run.app,SPRING_DATASOURCE_URL=jdbc:postgresql:///<DB> \
  --set-secrets SPRING_DATASOURCE_USERNAME=db-user:latest,SPRING_DATASOURCE_PASSWORD=db-pass:latest \
  --min-instances=1 --max-instances=2 \
  --cpu=1 --memory=512Mi
```
> 依需求設定 **Concurrency**、Autoscaling、VPC 連線（至 Cloud SQL）。

### 連線 Cloud SQL（JDBC）
- 透過 **Cloud SQL Auth Proxy** 或 **Serverless VPC Connector** 建立連線。
- `SPRING_DATASOURCE_URL` 形式可參考：
  - Proxy：`jdbc:postgresql://127.0.0.1:5432/<db>`
  - 直連（私網）：`jdbc:postgresql://<ip>:5432/<db>`

---

## CI/CD
- 使用 **GitHub Actions** 進行建置、測試與部署：
  - `deploy-java-backend.yml`：建置與部署 Spring Boot。
  - `deploy-nlp-service.yml`：建置與部署 NLPService。
- 建議在 workflow 中：
  - 進行單元測試與靜態檢查（Spotless/Checkstyle/PMD/SpotBugs）。
  - 產生 Docker 映像並推送至 Artifact Registry。
  - 以 `gcloud run deploy` 自動部署至指定環境。

---

## 非同步與高併發
- `@Async` + `CompletableFuture`：Webhook 主流程快速返回，NLP 與 domain handler 於背景執行。
- 建議自訂 `ThreadPoolTaskExecutor`：設定核心/最大執行緒數、佇列大小、命名慣例，以利觀測。
- Network call（LINE Messaging / NLP）建議避免 `.get()` 阻塞；若需等待結果，加入超時與重試策略。

---

## 日誌與觀測性
- **SLF4J** 統一日誌；錯誤使用 `log.error`；重要節點（解析/建構/呼叫外部 API）加上耗時 log。
- **追蹤**：MDC/traceId 於非同步傳遞（e.g., `TaskDecorator`）。
- **Metrics**：可導入 Micrometer + Prometheus/Grafana；針對 NlpClient 記錄重試次數、失敗率。

---

## 安全性
- 驗證 LINE 簽章，防止未授權請求。
- 秘密資訊以 Secret Manager / 環境變數管理，避免硬編碼。
- Cloud Run 僅允許必要公開端點；對內資源（DB）採私網/VPC。

---

## 待辦與 Roadmap
- [ ] 查詢作業改以 Push 模式回覆，降低 Webhook 延遲。
- [ ] FlexMessage 分頁與分段載入。
- [ ] 一次輸入多條待辦（多行文字解析）：
  ```
  9/21 跟小明開會
  9/26 出差
  10/1 家庭旅遊
  ```
- [ ] 體重模組完善
- [ ] 記帳模組完善
- [ ] MDC/traceId 在非同步鏈路的完整傳遞。


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
│       │   └── com/example/maco/
│       │       ├── LineEchoBotApplication.java
│       │       ├── linebot/
│       │       │   ├── LineBotController.java
│       │       │   ├── LineService.java
│       │       │   ├── model/
│       │       │   │   ├── ProcessRequest.java
│       │       │   │   ├── ProcessResponse.java
│       │       │   │   ├── RouterRequest.java
│       │       │   │   └── RouterResponse.java
│       │       │   └── util/NlpClient.java
│       │       ├── domain/
│       │       │   ├── dto/
│       │       │   │   ├── BaseResultDto.java
│       │       │   │   ├── LineMessageDto.java
│       │       │   │   └── TodoResultDto.java
│       │       │   ├── model/
│       │       │   │   ├── BaseResult.java
│       │       │   │   ├── todo/TodoResult.java
│       │       │   │   └── user/
│       │       │   └── port/user/
│       │       ├── infra/jpa/
│       │       │   ├── adapter/JpaTodoRepository.java
│       │       │   ├── entity/
│       │       │   │   ├── TodoEntity.java
│       │       │   │   ├── UserEntity.java
│       │       │   │   └── LineMessageEntity.java
│       │       │   ├── mapper/TodoMapper.java
│       │       │   └── repo/
│       │       │       ├── TodoJpaRepo.java
│       │       │       └── UserJpaRepo.java
│       │       ├── todo/TodoService.java
│       │       └── user/
│       │           ├── LineMessageService.java
│       │           └── UserService.java
│       └── resources/application.properties
└── target/
```

---

## 授權
本專案採用 [MIT License](./LICENSE)。

