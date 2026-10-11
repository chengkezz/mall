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

## 新增商品

`POST http://localhost:8080/products`，Header 使用 `Content-Type: application/json`。

```json
{
  "productName": "蘋果",
  "category": "FOOD",
  "imageUrl": "https://example.com/apple.jpg",
  "price": 20,
  "stock": 10,
  "description": "這是來自澳洲的蘋果！"
}
```

- 成功回傳 `201` 與完整商品 JSON，包含資料庫產生的 `productId` 和後端設定的時間。
- `productName`、`imageUrl` 不可空白，分別最多 128、256 字元。
- `category` 必填，使用 `FOOD`、`CAR` 或 `E_BOOK`。
- `price`、`stock` 必填，使用非負整數。
- `description` 可省略或為 null，最多 1024 字元。
- 缺少必填欄位、不合法分類、負數或超過長度限制時回傳 `400`。

執行 `MALL_DB_TEST=true ./mvnw test` 可額外驗證本機 MySQL 的新增與讀回。整合測試會回滾資料，但 MySQL 自增編號仍可能跳號；一般 `./mvnw test` 會跳過這項資料庫寫入測試。

## 修改商品

`PUT http://localhost:8080/products/{productId}`，Header 使用 `Content-Type: application/json`。

Body 與新增商品相同，需提供全部必填欄位。省略 `description` 或傳入 null 會清空描述。

- 成功回傳 `200` 與更新後完整商品 JSON。
- 商品不存在回傳 `404`；存在商品的輸入未通過驗證回傳 `400`。
- 保留商品 ID 與建立時間，更新最後修改時間（資料庫精度為秒）。
- 同內容重複修改仍應成功；目前 MySQL 連線採預設的匹配筆數回報方式，請勿改成 `useAffectedRows=true`，避免將未變動資料誤判為不存在。

`MALL_DB_TEST=true ./mvnw test` 也會驗證修改欄位、清空描述、建立時間保留、其他商品不受影響與重複修改。測試操作會回滾。

## 刪除商品

`DELETE http://localhost:8080/products/{productId}`，不需要 Request Body。

直接刪除指定商品。無論商品存在、不存在或重複刪除，SQL 成功執行後都回傳 `204 No Content`，沒有回應內容。ID 不是整數時回傳 `400`；資料庫執行錯誤不會被當成刪除成功。

`MALL_DB_TEST=true ./mvnw test` 會驗證刪除後查不到商品、其他商品仍存在，以及重複刪除回傳 204。只操作測試建立的商品，並在測試後回滾。
