-- ============================================================
-- 二手家具交易平台 · MVP 建表脚本
-- 方言：MySQL 8 兼容（H2 以 MODE=MySQL 运行同一份脚本）
-- 说明：这是唯一事实来源，后端启动时（dev profile）自动执行。
-- ============================================================

DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS item_image;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS order_logistics;
DROP TABLE IF EXISTS payments;

-- ------------------------------------------------------------
-- 用户
-- ------------------------------------------------------------
CREATE TABLE app_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone       VARCHAR(20)  NOT NULL,
    nickname    VARCHAR(30)  NOT NULL,
    avatar      VARCHAR(255),
    password    VARCHAR(100) NOT NULL DEFAULT '',
    role        VARCHAR(20)  NOT NULL DEFAULT 'both',
    credit_score INT         NOT NULL DEFAULT 100,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_user_phone ON app_user (phone);

-- ------------------------------------------------------------
-- 商品
-- ------------------------------------------------------------
CREATE TABLE item (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id        BIGINT        NOT NULL,
    category_id      BIGINT        NOT NULL,
    title            VARCHAR(60)   NOT NULL,
    description      VARCHAR(500),
    item_condition   TINYINT       NOT NULL,
    price            DECIMAL(10,2) NOT NULL,
    length_cm        INT,
    width_cm         INT,
    height_cm        INT,
    material         VARCHAR(20),
    style            VARCHAR(20),
    brand            VARCHAR(40),
    trade_type       TINYINT       NOT NULL DEFAULT 3,
    district_code    VARCHAR(12)   NOT NULL,
    address          VARCHAR(200),
    cover_url        VARCHAR(255),
    status           VARCHAR(20)   NOT NULL DEFAULT 'ON_SALE',
    weight_kg        DECIMAL(8,2),
    carbon_saved_kg  DECIMAL(8,2),
    recycle_flag     TINYINT       NOT NULL DEFAULT 0,
    view_count       INT           NOT NULL DEFAULT 0,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_item_status_created ON item (status, created_at);
CREATE INDEX idx_item_category       ON item (category_id);
CREATE INDEX idx_item_seller         ON item (seller_id);
CREATE INDEX idx_item_price          ON item (price);

-- ------------------------------------------------------------
-- 商品图片
-- ------------------------------------------------------------
CREATE TABLE item_image (
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT       NOT NULL,
    url     VARCHAR(255) NOT NULL,
    sort    INT          NOT NULL DEFAULT 0
);
CREATE INDEX idx_image_item ON item_image (item_id);

-- ------------------------------------------------------------
-- 订单
-- ------------------------------------------------------------
CREATE TABLE orders (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no         VARCHAR(32)   NOT NULL,
    item_id          BIGINT        NOT NULL,
    buyer_id         BIGINT        NOT NULL,
    seller_id        BIGINT        NOT NULL,
    item_title       VARCHAR(60),
    item_cover       VARCHAR(255),
    item_price       DECIMAL(10,2) NOT NULL,
    delivery_fee     DECIMAL(10,2) NOT NULL DEFAULT 0,
    floor_fee        DECIMAL(10,2) NOT NULL DEFAULT 0,
    assembly_fee     DECIMAL(10,2) NOT NULL DEFAULT 0,
    service_fee      DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount     DECIMAL(10,2) NOT NULL,
    delivery_type    TINYINT       NOT NULL,
    address_snapshot VARCHAR(500),
    status           VARCHAR(20)   NOT NULL,
    pay_expire_at    TIMESTAMP,
    paid_at          TIMESTAMP,
    shipped_at       TIMESTAMP,
    completed_at     TIMESTAMP,
    remark           VARCHAR(200),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_order_no ON orders (order_no);
CREATE INDEX idx_order_buyer  ON orders (buyer_id, created_at);
CREATE INDEX idx_order_seller ON orders (seller_id, created_at);
CREATE INDEX idx_order_status ON orders (status);

-- ------------------------------------------------------------
-- 物流预约
-- ------------------------------------------------------------
CREATE TABLE order_logistics (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no          VARCHAR(32) NOT NULL,
    provider          VARCHAR(30),
    provider_order_no VARCHAR(64),
    schedule_date     DATE,
    schedule_slot     VARCHAR(20),
    driver_name       VARCHAR(30),
    driver_phone      VARCHAR(64),
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING_DISPATCH',
    sign_photo        VARCHAR(255),
    reschedule_count  INT         NOT NULL DEFAULT 0,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_logistics_order ON order_logistics (order_no);

-- ------------------------------------------------------------
-- 支付流水
-- ------------------------------------------------------------
CREATE TABLE payments (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no    VARCHAR(32)   NOT NULL,
    channel     VARCHAR(20)   NOT NULL,
    trade_no    VARCHAR(64),
    amount      DECIMAL(10,2) NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    paid_at     TIMESTAMP,
    refunded_at TIMESTAMP,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_pay_order ON payments (order_no);

-- ------------------------------------------------------------
-- 刷新令牌（替代 Redis 存 token：能撤销、可持久化、无需新增中间件）
-- 只存 SHA-256 摘要，库被拖走也拿不到可用 token
-- ------------------------------------------------------------
CREATE TABLE refresh_token (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    revoked    TINYINT     NOT NULL DEFAULT 0,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_refresh_hash ON refresh_token (token_hash);
CREATE INDEX idx_refresh_user ON refresh_token (user_id);
