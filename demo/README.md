# Mall API

使用 Java 17 以上版本、Spring Boot 與 MySQL。

## 本機設定

1. 將 `src/main/resources/application.properties.example` 複製為同目錄的 `application.properties`。
2. 填入本機 MySQL 資料庫名稱、帳號與密碼。`application.properties` 已被 Git 忽略，請勿提交真實密碼。
3. 執行 `./mvnw test` 驗證專案。
4. 執行 `DemoApplication`，或使用 `./mvnw spring-boot:run` 啟動。
