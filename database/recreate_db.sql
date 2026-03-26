-- =============================================
-- FILE: database/recreate_db.sql
-- MỤC ĐÍCH: Làm sạch và khởi tạo lại toàn bộ cấu trúc BẢNG theo schema chuẩn
-- =============================================
SET NOCOUNT ON;

-- 1. XÓA TẤT CẢ CÁC RÀNG BUỘC KHÓA NGOẠI (Foreign Keys)
DECLARE @sql_fk NVARCHAR(MAX) = N'';
SELECT @sql_fk += 'ALTER TABLE ' + QUOTENAME(SCHEMA_NAME(schema_id)) + '.' + QUOTENAME(OBJECT_NAME(parent_object_id)) + 
                  ' DROP CONSTRAINT ' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.foreign_keys;

IF @sql_fk <> '' EXEC sp_executesql @sql_fk;
GO

-- 2. XÓA TẤT CẢ CÁC BẢNG (Tables)
DECLARE @sql_tables NVARCHAR(MAX) = N'';
SELECT @sql_tables += 'DROP TABLE ' + QUOTENAME(SCHEMA_NAME(schema_id)) + '.' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.tables;

IF @sql_tables <> '' EXEC sp_executesql @sql_tables;
GO

-- 3. KHỞI TẠO LẠI CẤU TRÚC BẢNG (Theo thứ tự ưu tiên phụ thuộc)

-- 3.1 Danh mục & Loại tài sản
CREATE TABLE category (
    category_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    category_name NVARCHAR(255) NOT NULL,
    description   NVARCHAR(255) NULL,
    status        NVARCHAR(40)  NOT NULL
);

CREATE TABLE asset_type (
    asset_type_id               INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    type_name                   NVARCHAR(255) NOT NULL,
    description                 NVARCHAR(255) NULL,
    type_class                  NVARCHAR(255) NOT NULL,
    status                      NVARCHAR(40)  NOT NULL,
    default_depreciation_method NVARCHAR(30)  NULL,
    default_useful_life_months  INT NULL,
    specification               NVARCHAR(255) NULL,
    category_id                 INT NOT NULL REFERENCES category(category_id),
    model                       NVARCHAR(255) NULL
);

-- 3.2 Nhóm tổ chức & Người dùng
CREATE TABLE departments (
    department_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    department_name NVARCHAR(150) NOT NULL,
    manager_user_id INT NULL, 
    status          NVARCHAR(40) NOT NULL DEFAULT N'ACTIVE',
    created_date    DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date    DATETIME NULL,
    description     NVARCHAR(1000) NULL
);

CREATE TABLE users (
    user_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username       NVARCHAR(50)  NOT NULL UNIQUE,
    password_hash  NVARCHAR(255) NOT NULL,
    first_name     NVARCHAR(150) NOT NULL,
    last_name      NVARCHAR(150) NOT NULL,
    phone_number   NVARCHAR(30)  NULL,
    email          NVARCHAR(100) NULL UNIQUE,
    status         NVARCHAR(40)  NOT NULL,
    role           NVARCHAR(40)  NOT NULL,
    created_date   DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date   DATETIME NULL,
    department_id  INT NOT NULL REFERENCES departments(department_id)
);

-- Thêm ràng buộc vòng giữa departments và users
ALTER TABLE departments ADD CONSTRAINT FK_departments_manager FOREIGN KEY (manager_user_id) REFERENCES users(user_id);

-- 3.3 Quy trình mua sắm (Purchasing)
CREATE TABLE supplier (
    supplier_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    supplier_name NVARCHAR(255) NOT NULL,
    phone_number  NVARCHAR(255) NOT NULL,
    email         NVARCHAR(255) NOT NULL,
    address       NVARCHAR(255) NOT NULL,
    supplier_code NVARCHAR(255) NULL,
    tax_code      NVARCHAR(255) NULL UNIQUE,
    status        NVARCHAR(255) NOT NULL,
    created_date  DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date  DATETIME NULL
);

CREATE TABLE purchase_request (
    purchase_request_id      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    status                   NVARCHAR(40)  NOT NULL,
    request_reason           NVARCHAR(255) NOT NULL,
    note                     NVARCHAR(255) NULL,
    creator_id               INT NOT NULL REFERENCES users(user_id),
    needed_by_date           DATETIME2(0) NOT NULL,
    priority                 NVARCHAR(255) NOT NULL,
    approved_by_director_id  INT NULL REFERENCES users(user_id),
    approved_by_director_at  DATETIME2(0) NULL,
    reject_reason            NVARCHAR(255) NULL,
    created_at               DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at               DATETIME2(0) NULL
);

CREATE TABLE purchase_request_detail (
    purchase_request_detail_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    estimated_price            NUMERIC(19) NOT NULL,
    quantity                   INT NOT NULL,
    purchase_request_id        INT NOT NULL REFERENCES purchase_request(purchase_request_id),
    asset_type_id              INT NOT NULL REFERENCES asset_type(asset_type_id),
    spec_requirement           NVARCHAR(255) NOT NULL,
    note                       NVARCHAR(255) NULL,
    created_at                 DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at                 DATETIME2(0) NULL
);

CREATE TABLE quotation (
    quotation_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    purchase_request_id INT NOT NULL REFERENCES purchase_request(purchase_request_id),
    supplier_id         INT NOT NULL REFERENCES supplier(supplier_id),
    status              NVARCHAR(255) NOT NULL,
    total_amount        NUMERIC(19) NULL,
    created_at          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at          DATETIME2(0) NULL
);

CREATE TABLE quotation_detail (
    quotation_detail_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    quotation_id               INT NOT NULL REFERENCES quotation(quotation_id),
    purchase_request_detail_id INT NOT NULL REFERENCES purchase_request_detail(purchase_request_detail_id),
    asset_type_id              INT NOT NULL REFERENCES asset_type(asset_type_id),
    quantity                   INT NOT NULL,
    quotation_detail_note      NVARCHAR(255) NULL,
    warranty_months            INT NULL,
    price                      NUMERIC(19) NOT NULL,
    discount_rate              DECIMAL(5, 2) NOT NULL DEFAULT 0,
    tax_rate                   DECIMAL(5, 2) NOT NULL DEFAULT 0,
    status                     NVARCHAR(100) NULL,
    spec_requirement           NVARCHAR(255) NULL,
    created_at                 DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at                 DATETIME2(0) NULL
);

CREATE TABLE purchase_orders (
    purchase_order_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    total_amount        NUMERIC(19) NOT NULL,
    note                NVARCHAR(255) NULL,
    status              NVARCHAR(40) NOT NULL,
    purchase_request_id INT NOT NULL REFERENCES purchase_request(purchase_request_id),
    supplier_id         INT NOT NULL REFERENCES supplier(supplier_id),
    quotation_id        INT NOT NULL REFERENCES quotation(quotation_id),
    approved_by         INT NULL REFERENCES users(user_id),
    updated_by          INT NULL REFERENCES users(user_id),
    created_at          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at          DATETIME2(0) NULL
);

CREATE TABLE purchase_order_details (
    purchase_order_detail_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    quantity               INT NOT NULL,
    unit_price             NUMERIC(19) NOT NULL,
    tax_rate               NUMERIC(5, 2) NULL,
    purchase_order_id      INT NOT NULL REFERENCES purchase_orders(purchase_order_id),
    asset_type_id          INT NOT NULL REFERENCES asset_type(asset_type_id),
    discount               NUMERIC(19) NULL,
    note                   NVARCHAR(255) NULL,
    quotation_detail_id    INT NULL REFERENCES quotation_detail(quotation_detail_id),
    delivery_date          DATETIME2(0) NULL,
    created_at             DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at             DATETIME2(0) NULL
);

-- 3.4 Tài sản (Core Asset)
CREATE TABLE wh_inbound_receipt (
    receipt_id        INT IDENTITY(1,1) PRIMARY KEY,
    purchase_order_id INT NOT NULL REFERENCES purchase_orders(purchase_order_id),
    delivery_note     NVARCHAR(255) NULL, -- Số phiếu giao hàng từ NCC
    received_by       INT NOT NULL REFERENCES users(user_id),
    received_at       DATETIME2(0) DEFAULT SYSDATETIME(),
    note              NVARCHAR(MAX) NULL
);

CREATE TABLE wh_inbound_receipt_detail (
    receipt_detail_id        INT IDENTITY(1,1) PRIMARY KEY,
    receipt_id               INT NOT NULL REFERENCES wh_inbound_receipt(receipt_id),
    purchase_order_detail_id INT NOT NULL REFERENCES purchase_order_details(purchase_order_detail_id),
    asset_type_id            INT NOT NULL REFERENCES asset_type(asset_type_id),
    quantity_received        INT NOT NULL
);

CREATE TABLE asset (
    asset_id                 INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    asset_name               NVARCHAR(100) NOT NULL,
    asset_type_id            INT NOT NULL REFERENCES asset_type(asset_type_id),
    purchase_order_detail_id INT NULL REFERENCES purchase_order_details(purchase_order_detail_id),
    receipt_detail_id        INT NULL REFERENCES wh_inbound_receipt_detail(receipt_detail_id),
    current_status           NVARCHAR(40) NOT NULL,
    original_cost            NUMERIC(19, 2) NULL,
    department_id            INT NULL REFERENCES departments(department_id),
    acquisition_date         DATE NULL,
    in_service_date          DATE NULL,
    warranty_start_date      DATE NULL,
    warranty_end_date        DATE NULL
);

-- 3.5 Cấp phát (Allocation)
CREATE TABLE allocation_request (
    request_id              INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    requester_id            INT NOT NULL REFERENCES users(user_id),
    requested_department_id INT NOT NULL REFERENCES departments(department_id),
    request_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    needed_by_date          DATE NULL,
    priority                NVARCHAR(20) NOT NULL,
    reason                  NVARCHAR(500) NOT NULL,
    status                  NVARCHAR(40) NOT NULL,
    am_approved_by          INT NULL REFERENCES users(user_id),
    am_approved_at          DATETIME2(0) NULL,
    reason_reject           NVARCHAR(255) NULL,
    created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at              DATETIME2(0) NULL
);

CREATE TABLE allocation_request_detail (
    request_detail_id    INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    request_id           INT NOT NULL REFERENCES allocation_request(request_id),
    asset_type_id        INT NOT NULL REFERENCES asset_type(asset_type_id),
    quantity_requested   INT NOT NULL,
    note                 NVARCHAR(255) NULL
);

-- 3.6 Yêu cầu trả (Return)
CREATE TABLE return_request (
    request_id              INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    requester_id            INT NOT NULL REFERENCES users(user_id),
    requested_department_id INT NOT NULL REFERENCES departments(department_id),
    request_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    reason                  NVARCHAR(500) NOT NULL,
    status                  NVARCHAR(40) NOT NULL,
    wh_confirmed_by          INT NULL REFERENCES users(user_id),
    wh_confirmed_at          DATETIME2(0) NULL,
    created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at              DATETIME2(0) NULL
);

CREATE TABLE return_request_detail (
    request_detail_id    INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    request_id           INT NOT NULL REFERENCES return_request(request_id),
    asset_id			 INT NOT NULL REFERENCES asset(asset_id),
    note                 NVARCHAR(255) NULL
);

-- 3.7 Lệnh thực hiện (Handover)
CREATE TABLE asset_handover (
    handover_id             INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    handover_type           NVARCHAR(40) NOT NULL,
    allocation_request_id   INT NULL REFERENCES allocation_request(request_id),
    return_request_id       INT NULL REFERENCES return_request(request_id),
    from_department_id      INT NULL REFERENCES departments(department_id),
    to_department_id        INT NULL REFERENCES departments(department_id),
    executed_by_user_id     INT NOT NULL REFERENCES users(user_id),
    handover_date           DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    status                  NVARCHAR(40) NOT NULL,
    note                    NVARCHAR(500) NULL,
    created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at              DATETIME2(0) NULL
);

CREATE TABLE asset_handover_detail (
    handover_detail_id      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    handover_id             INT NOT NULL REFERENCES asset_handover(handover_id),
    asset_id                INT NOT NULL REFERENCES asset(asset_id),
    condition_status        NVARCHAR(100) NULL,
    note                    NVARCHAR(255) NULL
);

-- 3.8 Điều chuyển (Transfer)
CREATE TABLE transfer_request (
    transfer_id             INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    allocation_request_id   INT NULL REFERENCES allocation_request(request_id),
    from_department_id      INT NULL REFERENCES departments(department_id),
    to_department_id        INT NULL REFERENCES departments(department_id),
    asset_manager_id        INT NULL REFERENCES users(user_id),
    transfer_date           DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    reason                  NVARCHAR(500) NULL,
    status                  NVARCHAR(40) NOT NULL,
    sender_confirmed_by     INT NULL REFERENCES users(user_id),
    sender_confirmed_at     DATETIME2(0) NULL,
    receiver_confirmed_by   INT NULL REFERENCES users(user_id),
    receiver_confirmed_at   DATETIME2(0) NULL,
    created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at              DATETIME2(0) NULL
);

CREATE TABLE transfer_request_detail (
    transfer_detail_id           INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    transfer_id                  INT NOT NULL REFERENCES transfer_request(transfer_id),
    allocation_request_detail_id INT NULL REFERENCES allocation_request_detail(request_detail_id),
    asset_id                     INT NOT NULL REFERENCES asset(asset_id),
    condition_from_sender        NVARCHAR(40) NULL,
    note                         NVARCHAR(255) NULL
);

-- 3.9 QC Report
CREATE TABLE qc_report (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    asset_id     INT NOT NULL REFERENCES asset(asset_id),
    qc_status    NVARCHAR(40) NOT NULL,
    inspected_by INT NOT NULL REFERENCES users(user_id),
    qc_date      DATETIME2 DEFAULT SYSDATETIME(),
    note         NVARCHAR(MAX)
);

-- 3.10 Module Kho: Setup không gian
CREATE TABLE wh_warehouses (
    warehouse_id    INT IDENTITY(1,1) PRIMARY KEY,
    name            NVARCHAR(100) NOT NULL,
    address         NVARCHAR(255) NOT NULL,
    manager_user_id INT NOT NULL REFERENCES users(user_id),
    status          NVARCHAR(40) DEFAULT N'ACTIVE'
);

CREATE TABLE wh_asset_capacity (
    asset_type_id   INT PRIMARY KEY REFERENCES asset_type(asset_type_id),
    unit_volume     INT NOT NULL DEFAULT 1
);

CREATE TABLE wh_zones (
    zone_id          INT IDENTITY(1,1) PRIMARY KEY,
    warehouse_id     INT NOT NULL REFERENCES wh_warehouses(warehouse_id),
    zone_name        NVARCHAR(100) NOT NULL,
    max_capacity     INT NOT NULL,
    current_capacity INT NOT NULL DEFAULT 0,
    asset_type_id    INT NULL REFERENCES asset_type(asset_type_id),
    status           NVARCHAR(40) DEFAULT N'ACTIVE'
);

CREATE TABLE wh_asset_placement (
    asset_id        INT PRIMARY KEY REFERENCES asset(asset_id),
    zone_id         INT NOT NULL REFERENCES wh_zones(zone_id),
    placed_by       INT NOT NULL REFERENCES users(user_id),
    placed_at       DATETIME2(0) DEFAULT SYSDATETIME(),
    note            NVARCHAR(255) NULL
);

CREATE TABLE wh_transactions (
    transaction_id   INT IDENTITY(1,1) PRIMARY KEY,
    asset_id         INT NOT NULL REFERENCES asset(asset_id),
    zone_id          INT NOT NULL REFERENCES wh_zones(zone_id),
    transaction_type NVARCHAR(20) NOT NULL,
    executed_by      INT NOT NULL REFERENCES users(user_id),
    executed_at      DATETIME2(0) DEFAULT SYSDATETIME(),
    note             NVARCHAR(255) NULL
);

CREATE TABLE map_po_transactions (
    purchase_order_id INT NOT NULL REFERENCES purchase_orders(purchase_order_id),
    transaction_id    INT NOT NULL REFERENCES wh_transactions(transaction_id),
    PRIMARY KEY (purchase_order_id, transaction_id)
);

CREATE TABLE map_handover_transactions (
    asset_handover_id INT NOT NULL REFERENCES asset_handover(handover_id),
    transaction_id    INT NOT NULL REFERENCES wh_transactions(transaction_id),
    PRIMARY KEY (asset_handover_id, transaction_id)
);

PRINT 'All tables recreated successfully according to schema.';
