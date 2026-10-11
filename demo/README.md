# Mall API

使用 Java 17 以上版本、Spring Boot 與 MySQL。

## 本機設定

1. 將 `src/main/resources/application.properties.example` 複製為同目錄的 `application.properties`。
2. 填入本機 MySQL 資料庫名稱、帳號與密碼。`application.properties` 已被 Git 忽略，請勿提交真實密碼。
3. 執行 `./mvnw test` 驗證專案。
4. 執行 `DemoApplication`，或使用 `./mvnw spring-boot:run` 啟動。

## 查詢單一商品

`GET http://localhost:8080/products/{productId}`

- 商品存在：回傳 `200` 與商品 JSON。
- 商品不存在：回傳 `404`，無回應內容。
- ID 不是整數：回傳 `400`。

採用 Controller → Service → DAO 分層，透過 JDBC 查詢，使用 `@Autowired` 欄位注入。

此功能使用既有的 `product` 資料表，不會自動建表。資料表需包含：

| 欄位 | 型別 |
| --- | --- |
| product_id | INT PRIMARY KEY AUTO_INCREMENT |
| product_name | VARCHAR(128) NOT NULL |
| category | VARCHAR(32) NOT NULL |
| image_url | VARCHAR(256) NOT NULL |
| price | INT NOT NULL |
| stock | INT NOT NULL |
| description | VARCHAR(1024)，允許 NULL |
| created_date | TIMESTAMP NOT NULL |
| last_modified_date | TIMESTAMP NOT NULL |

Controller 測試使用模擬的 Service；實際資料庫查詢需另外啟動 API 驗證。

## 日期回傳格式

商品的 `createdDate` 與 `lastModifiedDate` 以 GMT+8、`yyyy-MM-dd HH:mm:ss` 格式回傳，例如 `2022-03-01 02:41:28`。此設定只影響 JSON 日期呈現，不修改資料庫資料。

若已有本機 `application.properties`，請同步加入範本中的 `spring.jackson.date-format` 與 `spring.jackson.time-zone` 設定，並重新啟動程式。
