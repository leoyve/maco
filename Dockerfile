# 多階段建置 Dockerfile（從原始碼建置 Spring Boot 可執行 JAR）
# 使用範例：
# 1) 在專案根目錄下建置映像：
#    docker build -t maestro-java-backend:latest .
# 2) 執行映像：
#    docker run --rm -e SPRING_DATASOURCE_URL="jdbc:postgresql://db:5432/dbname" -p 8080:8080 maestro-java-backend:latest

# 建置階段：使用 Maven image 來編譯並打包
FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /workspace
# 複製 pom.xml 先解決依賴快取
COPY pom.xml ./
RUN mvn dependency:go-offline
# 如果有 settings.xml 或其他設定，可一併複製
# 複製原始碼並打包
COPY src ./src
RUN mvn -B -DskipTests package --no-transfer-progress

# 執行階段：使用較小的 JRE 映像
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# 將建置階段的 JAR 複製到執行映像
COPY --from=build /workspace/target/*.jar app.jar

# 建議設定預設環境變數（可由 docker run 或 k8s 覆寫）
ENV SPRING_PROFILES_ACTIVE=dev \
    JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

# 非互動式的啟動指令，允許通過 JAVA_OPTS 傳入額外 JVM 參數
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

# 可選健康檢查（Docker 版本支援）
# HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
