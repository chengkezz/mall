# Spring Boot Mall｜電商後端 REST API

以 **Java、Spring Boot、Spring JDBC 與 MySQL** 實作的電商後端練習專案，涵蓋商品管理、會員註冊與登入、訂單建立及查詢。採用 Controller → Service → DAO 分層，透過手寫 SQL 練習資料存取、條件查詢與交易控制。

本專案提供 JSON API，可使用 curl 或 Postman 操作，尚未包含前端購物介面。

## 功能概覽

| 模組 | 已實作功能 |
| --- | --- |
| 商品 | 新增、查詢、修改、刪除商品 |
| 商品列表 | 分類篩選、名稱關鍵字搜尋、價格／建立時間排序、分頁與總筆數 |
| 會員 | Email 註冊、重複帳號檢查、帳密驗證、回應隱藏密碼 |
| 訂單 | 建立訂單、合併重複商品、計算金額、扣減庫存、查詢使用者訂單與明細 |
| 驗證與測試 | 輸入驗證、MockMvc／Mockito 測試、可選擇執行的 MySQL 整合測試 |

## 技術與架構

| 技術 | 用途 |
| --- | --- |
| Java 17+ | 程式語言，`pom.xml` 設定目標版本為 17 |
| Spring Boot 4.1.1 / Spring MVC | 應用程式啟動與 REST API |
| Spring JDBC | 透過 `NamedParameterJdbcTemplate` 執行 SQL |
| MySQL | 儲存商品、使用者、訂單與明細 |
| Jakarta Validation | 驗證會員輸入、訂單內容與分頁參數 |
| Maven Wrapper | 建置、測試與啟動 |
| JUnit / Mockito / MockMvc | 單元測試與 HTTP 行為驗證 |

```text
HTTP Request
    ↓
Controller  接收參數、回傳 HTTP 狀態與 JSON
    ↓
Service     處理驗證、商業邏輯與交易
    ↓
DAO         使用具名參數執行 SQL
    ↓
MySQL       儲存資料，透過 RowMapper 轉成 Java 物件
```

```text
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── controller/    # 商品、會員、訂單 API
│   │   ├── service/       # 商業邏輯與實作
│   │   ├── dao/           # 資料存取介面與 JDBC 實作
│   │   ├── dto/           # 請求、查詢條件與分頁物件
│   │   ├── model/         # 商品、會員、訂單與明細
│   │   ├── rowmapper/     # SQL 查詢結果映射
│   │   ├── constant/      # 商品分類
│   │   └── exception/     # 驗證例外處理
│   └── resources/
│       └── application.properties.example
└── test/java/com/example/demo/
    ├── controller/
    ├── service/
    ├── dao/
    └── rowmapper/
```

### 訂單交易設計

建立訂單時，後端先確認使用者存在，再合併相同商品的購買數量，依商品 ID 順序處理庫存，並以資料庫中的價格計算金額。

- 使用 `@Transactional` 將扣庫存、建立訂單與寫入明細包在同一交易中，任一步驟失敗會回滾。
- 扣庫存 SQL 同時檢查 `stock >= quantity`，以更新筆數判斷商品是否可購買，避免單純先讀庫存再覆寫造成超扣。
- 使用 `Math.addExact` 與 `Math.multiplyExact` 檢查數量、單項金額及總金額的整數溢位。
- 訂單金額由後端計算，客戶端僅傳商品 ID 與數量。

## 本機啟動

### 1. 下載專案

準備 JDK 17 以上版本及可連線的 MySQL，再執行：

```bash
git clone https://github.com/chengkezz/mall.git
cd mall/demo
```

以下指令皆在 `demo` 目錄執行。Windows 可將 `./mvnw` 替換為 `mvnw.cmd`。

### 2. 建立資料庫與資料表

專案目前不會自動建表，也未提供資料庫 migration。以下為依現有 DAO 欄位整理的**本機建表範例**，請在新的 MySQL 練習資料庫執行；既有資料庫請先確認結構，不要直接覆蓋。

```sql
CREATE DATABASE mall CHARACTER SET utf8mb4;
USE mall;

CREATE TABLE product (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(128) NOT NULL,
    category VARCHAR(32) NOT NULL,
    image_url VARCHAR(256) NOT NULL,
    price INT NOT NULL,
    stock INT NOT NULL,
    description VARCHAR(1024),
    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `user` (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(256) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `order` (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    total_amount INT NOT NULL,
    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP NOT NULL,
    INDEX idx_order_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE order_item (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    amount INT NOT NULL,
    INDEX idx_order_item_order (order_id)
) ENGINE=InnoDB;
```

資料關聯為：使用者對應多張訂單、訂單對應多筆明細、每筆明細記錄商品 ID。上述範例未加外鍵；訂單明細會保留建立當時的數量與金額。

### 3. 設定 MySQL 連線

首次設定時，複製設定範本；若檔案已存在，直接編輯即可：

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

將 `YOUR_MYSQL_USERNAME`、`YOUR_MYSQL_PASSWORD` 替換為本機帳密；若資料庫名稱、主機或連接埠不同，也需調整 URL。

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/mall?serverTimezone=Asia/Taipei&characterEncoding=utf-8
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
spring.jackson.time-zone=GMT+8
```

`application.properties` 已被 Git 忽略，請勿提交真實帳密。日期 JSON 使用 GMT+8 的 `yyyy-MM-dd HH:mm:ss` 格式。

### 4. 啟動服務

```bash
./mvnw spring-boot:run
```

也可在 IntelliJ IDEA 開啟 `pom.xml`，等待 Maven 匯入完成後執行 `DemoApplication`。預設 API 位址為 `http://localhost:8080`。

## API 一覽

POST 與 PUT 的 JSON 請求須帶上 `Content-Type: application/json`。

| 方法 | 路徑 | 說明 | 成功狀態 |
| --- | --- | --- | --- |
| GET | `/products` | 篩選、排序及分頁查詢商品 | 200 |
| GET | `/products/{productId}` | 查詢單一商品 | 200 |
| POST | `/products` | 新增商品 | 201 |
| PUT | `/products/{productId}` | 修改商品 | 200 |
| DELETE | `/products/{productId}` | 刪除商品 | 204 |
| POST | `/users/register` | 註冊會員 | 201 |
| POST | `/users/login` | 核對登入帳密 | 200 |
| POST | `/users/{userId}/orders` | 建立訂單 | 201 |
| GET | `/users/{userId}/orders` | 查詢使用者訂單及明細 | 200 |

### 商品列表參數

| 參數 | 預設值 | 規則 |
| --- | --- | --- |
| `category` | 不限 | `FOOD`、`CAR`、`E_BOOK`，區分大小寫 |
| `search` | 不限 | 搜尋商品名稱，去除前後空白；空白視為未指定 |
| `orderBy` | `created_date` | 僅允許 `created_date`、`price` |
| `sort` | `desc` | 僅允許小寫 `asc`、`desc` |
| `limit` | `5` | 1～100 |
| `offset` | `0` | 大於等於 0，代表略過的筆數 |

```bash
curl 'http://localhost:8080/products?category=E_BOOK&search=Java&orderBy=price&sort=asc&limit=5&offset=0'
```

分類與關鍵字條件會同時套用；主要排序值相同時，以 `product_id ASC` 排序。搜尋使用 SQL `LIKE`，`%`、`_` 仍具有萬用字元意義。

回應為分頁物件，`productlist` 的大小寫依目前 API 命名。`total` 不受分頁影響，例如符合條件共 7 筆，但略過 10 筆時：

```json
{
  "limit": 5,
  "offset": 10,
  "total": 7,
  "productlist": []
}
```

### 商品新增與修改規則

- `productName`、`imageUrl` 不可空白，長度上限分別為 128、256 字元。
- `category` 必填，限上述三種分類。
- `price`、`stock` 必填，須為非負整數。
- `description` 可省略或為 `null`，最多 1024 字元。
- PUT 須提供全部必填欄位；省略 `description` 或傳入 `null` 會清空描述。
- 修改保留建立時間，更新最後修改時間。MySQL 連線請保留預設匹配筆數行為，勿設定 `useAffectedRows=true`，避免相同內容更新被誤判為不存在。
- 刪除成功回傳 `204` 且無內容，商品不存在或重複刪除也相同；資料庫錯誤不會視為成功。

## 操作範例：註冊 → 新增商品 → 下單 → 查詢

### 1. 註冊與登入

```bash
curl -X POST 'http://localhost:8080/users/register' \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"demo-password"}'

curl -X POST 'http://localhost:8080/users/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"demo-password"}'
```

Email 與密碼不可空白，Email 須符合格式。註冊成功回傳 `201`，登入成功回傳 `200`，內容包含 `userId`、`email` 與建立／修改時間，不包含密碼。請記下回傳的 `userId`。

### 2. 新增商品

```bash
curl -X POST 'http://localhost:8080/products' \
  -H 'Content-Type: application/json' \
  -d '{
    "productName": "Java 入門電子書",
    "category": "E_BOOK",
    "imageUrl": "https://example.com/java-book.jpg",
    "price": 500,
    "stock": 10,
    "description": "Java 基礎學習教材"
  }'
```

成功回傳 `201` 與完整商品資料，請記下 `productId`。`imageUrl` 是圖片網址欄位，目前沒有圖片上傳功能。

### 3. 建立訂單

將以下 `USER_ID` 與 `PRODUCT_ID` 換成前兩步實際回傳的整數 ID，再執行：

```bash
curl -X POST 'http://localhost:8080/users/USER_ID/orders' \
  -H 'Content-Type: application/json' \
  -d '{"buyItemList":[{"productId":PRODUCT_ID,"quantity":2}]}'
```

清單不可為空，商品 ID 與數量須為正整數。成功回傳 `201`，例如：

```json
{"orderId": 1}
```

以上述商品價格購買 2 件時，後端計算訂單金額為 1000，並將庫存由 10 扣為 8。

### 4. 查詢訂單

```bash
curl 'http://localhost:8080/users/USER_ID/orders'
```

回傳訂單陣列，每張訂單包含 `orderId`、`userId`、`totalAmount`、`createdDate`、`lastModifiedDate` 與 `orderItemList`。明細包含 `orderItemId`、`orderId`、`productId`、`quantity`、`amount`。

訂單依 `created_date DESC, order_id DESC` 排序，明細依 `order_item_id ASC` 排序。存在的使用者若沒有訂單，回傳 `200` 與 `[]`。

### 常見錯誤狀態

| 狀態 | 情境 |
| --- | --- |
| 400 | 參數驗證失敗、非法分類／排序、ID 不是整數、註冊 Email 已存在 |
| 400 | 下單商品不存在、庫存不足、數量或金額超過 INT 上限 |
| 401 | 登入 Email 不存在或密碼錯誤 |
| 404 | 查詢／修改的商品不存在，或下單／查訂單的使用者不存在 |

## 測試

一般測試：

```bash
./mvnw test
```

包含 Controller 的 HTTP 行為、Service 邏輯、RowMapper 與應用程式 Context 載入測試。MySQL 整合測試預設跳過；Context 載入仍需要可用的 datasource 設定。

需要驗證真實 MySQL 存取時，先完成建表與連線設定，建議使用獨立的測試資料庫：

```bash
MALL_DB_TEST=true ./mvnw test
```

也可指定訂單相關測試：

```bash
MALL_DB_TEST=true ./mvnw -Dtest=OrderIntegrationTest,OrderQueryIntegrationTest test
```

整合測試涵蓋商品新增／修改／刪除／分類、會員註冊、訂單與庫存回滾，以及訂單查詢。測試使用交易回滾或清理自行建立的資料；MySQL 自增 ID 仍可能跳號。一般測試通過不代表 MySQL 整合測試已通過。

