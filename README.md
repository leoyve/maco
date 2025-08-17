# Spring Boot LINE Echo Bot

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue.svg)](https://maven.apache.org/)
[![LINE API](https://img.shields.io/badge/LINE%20API-Messaging-green.svg)](https://developers.line.biz/)

---

## 目錄
- [更新紀錄](#更新紀錄)
- [主要功能](#主要功能)
- [技術棧](#技術棧)
- [開始使用](#開始使用)
- [資料庫設定](#資料庫設定)
- [本機啟動](#本機啟動)
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