# Spring Boot LINE Echo Bot

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue.svg)](https://maven.apache.org/)
[![LINE API](https://img.shields.io/badge/LINE%20API-Messaging-green.svg)](https://developers.line.biz/)
[![Python](https://img.shields.io/badge/Python-3.10+-blue.svg)](https://www.python.org)

---

## 近期優化紀錄（2025/08/19）
- 分層架構全面優化：Controller 層只用 DTO，Service 層只用 Model，Repository 層只用 Entity，物件映射流程完整，維護性高。
- TODO 模組：已支援 NLPService 語意分析、DTO/Model/Entity分離、物件映射、JPA 儲存、createdAt/updatedAt 自動填入，新增代辦事項功能已完成。
- HEALTH 模組：已預留分層架構與 API 串接範例，未來可快速擴充健康紀錄、查詢等功能。
- TodoMapper.toEntity() 支援 NLPService 回傳的 "yyyy-MM-dd HH:mm" 時間格式，避免 DateTimeParseException。
- UserEntity、TodoEntity、LineMessageEntity 都已加入 JPA Auditing 註解，createdAt/updatedAt 欄位可自動填入時間戳。
- 高併發流程、DTO/Model/Entity分層、物件映射、異常處理、日誌等皆已優化。

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

## 專案結構

```
Maestro/
├── pom.xml
├── README.md
├── start-dev.sh
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── maco/
│       │               ├── LineEchoBotApplication.java
│       │               ├── adapters/
│       │               │   └── db/
│       │               │       └── jpa/
│       │               │           ├── LineUserMessage.java
│       │               │           ├── LineUserMessageMapper.java
│       │               │           └── LineUserMessageRepository.java
│       │               └── linebot/
│       │                   ├── LineBotController.java
│       │                   ├── LineService.java
│       │                   └── model/
│       │                       └── LineMessageDto.java
│       └── resources/
│           └── application.properties
└── target/
    └── ...
```

---

## 授權 (License)
本專案採用 MIT 授權。詳情請見 `LICENSE` 檔案。