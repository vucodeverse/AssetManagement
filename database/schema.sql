-- =============================================
-- 0. DỌN DẸP DỮ LIỆU CŨ (DROP TABLES)
-- =============================================
SET NOCOUNT ON;

-- Module Kho (Warehouse) - Xóa trước do có FK tham chiếu các bảng cơ bản
DROP TABLE IF EXISTS map_allocation_transactions;
DROP TABLE IF EXISTS map_return_transactions;
DROP TABLE IF EXISTS map_po_transactions;
DROP TABLE IF EXISTS wh_transactions;
DROP TABLE IF EXISTS wh_asset_placement;
DROP TABLE IF EXISTS wh_zones;
DROP TABLE IF EXISTS wh_asset_capacity;
DROP TABLE IF EXISTS wh_warehouses;

-- Module Giao dịch & Cấp phát
DROP TABLE IF EXISTS transfer_order_detail;
DROP TABLE IF EXISTS transfer_order;
DROP TABLE IF EXISTS asset_handover_detail;
DROP TABLE IF EXISTS asset_handover;
DROP TABLE IF EXISTS return_request_detail;
DROP TABLE IF EXISTS return_request;
DROP TABLE IF EXISTS allocation_request_detail;
DROP TABLE IF EXISTS allocation_request;

-- Module Tài sản & Mua sắm
DROP TABLE IF EXISTS qc_report;
DROP TABLE IF EXISTS asset;
DROP TABLE IF EXISTS purchase_order_details;
DROP TABLE IF EXISTS purchase_orders;
DROP TABLE IF EXISTS quotation_detail;
DROP TABLE IF EXISTS quotation;
DROP TABLE IF EXISTS purchase_request_detail;
DROP TABLE IF EXISTS purchase_request;
DROP TABLE IF EXISTS supplier;

-- Danh mục & Người dùng
-- Lưu ý: Xóa FK vòng trước khi xóa bảng departments/users
IF OBJECT_ID('departments', 'U') IS NOT NULL 
    ALTER TABLE departments DROP CONSTRAINT IF EXISTS FK_departments_manager;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS asset_type;
DROP TABLE IF EXISTS category;

-- =============================================
-- 1. DANH MỤC CỐT LÕI (CATEGORIES & ASSET TYPES)
-- =============================================

CREATE TABLE category (
    category_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    category_name NVARCHAR(255) NOT NULL,
    description   NVARCHAR(255) NULL,
    status        NVARCHAR(40)  NOT NULL -- ACTIVE, INACTIVE
);

CREATE TABLE asset_type (
    asset_type_id               INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    type_name                   NVARCHAR(255) NOT NULL,
    description                 NVARCHAR(255) NULL,
    type_class                  NVARCHAR(255) NOT NULL, -- FIXED_ASSET, TOOL, EQUIPMENT, CONSUMABLE, HARDWARE, SOFTWARE, ELECTRONICS, FURNITURE, IT_ASSET, OFFICE_ASSET
    status                      NVARCHAR(40)  NOT NULL, -- ACTIVE, INACTIVE
    default_depreciation_method NVARCHAR(30)  NULL, -- STRAIGHT_LINE, DECLINING_BALANCE
    default_useful_life_months  INT NULL,
    specification               NVARCHAR(255) NULL,
    category_id                 INT NOT NULL REFERENCES category(category_id),
    model                       NVARCHAR(255) NULL
);

-- =============================================
-- 2. NHÓM TỔ CHỨC & NGƯỜI DÙNG
-- =============================================

CREATE TABLE departments (
    department_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    department_name NVARCHAR(150) NOT NULL,
    manager_user_id INT NULL, -- Sẽ thêm constraint sau khi bảng users được tạo
    status          NVARCHAR(40) NOT NULL DEFAULT N'ACTIVE', -- ACTIVE, INACTIVE
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
    status         NVARCHAR(40)  NOT NULL, -- ACTIVE, INACTIVE
    role           NVARCHAR(40)  NOT NULL, -- ADMIN, PURCHASE_STAFF, ASSET_MANAGER, DEPARTMENT_MANAGER, WAREHOUSE_STAFF, DIRECTOR
    created_date   DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date   DATETIME NULL,
    department_id  INT NOT NULL REFERENCES departments(department_id)
);

-- Thêm ràng buộc vòng giữa departments và users
ALTER TABLE departments 
ADD CONSTRAINT FK_departments_manager FOREIGN KEY (manager_user_id) REFERENCES users(user_id);

CREATE UNIQUE INDEX UQ_departments_manager_user
    ON departments(manager_user_id)
    WHERE manager_user_id IS NOT NULL;

-- =============================================
-- 3. QUY TRÌNH MUA SẮM (PURCHASING)
-- =============================================

CREATE TABLE supplier (
    supplier_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    supplier_name NVARCHAR(255) NOT NULL,
    phone_number  NVARCHAR(255) NOT NULL,
    email         NVARCHAR(255) NOT NULL,
    address       NVARCHAR(255) NOT NULL,
    supplier_code NVARCHAR(255) NULL,
    tax_code      NVARCHAR(255) NULL UNIQUE,
    status        NVARCHAR(255) NOT NULL, -- ACTIVE, INACTIVE
    created_date  DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date  DATETIME NULL
);

CREATE TABLE purchase_request (
    purchase_request_id      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    status                   NVARCHAR(40)  NOT NULL, -- DRAFT, PENDING, APPROVED, ORDERED, REJECTED, DELETED
    request_reason           NVARCHAR(255) NULL,
    note                     NVARCHAR(255) NULL,
    creator_id               INT NOT NULL REFERENCES users(user_id),
    requesting_department_id INT NULL REFERENCES departments(department_id),
    needed_by_date           DATETIME2(0) NULL,
    priority                 NVARCHAR(255) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
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
    spec_requirement           NVARCHAR(255) NULL,
    note                       NVARCHAR(255) NULL
);

CREATE TABLE quotation (
    quotation_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    purchase_request_id INT NOT NULL REFERENCES purchase_request(purchase_request_id),
    supplier_id         INT NOT NULL REFERENCES supplier(supplier_id),
    status              NVARCHAR(255) NOT NULL, -- DRAFT, PENDING, APPROVED, REJECTED, DELETED
    total_amount        NUMERIC(19) NULL,
    created_at          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at          DATETIME2(0) NULL,
    reject_reason       NVARCHAR(255) NULL
);

CREATE TABLE quotation_detail (
    quotation_detail_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    quotation_id               INT NOT NULL REFERENCES quotation(quotation_id),
    purchase_request_detail_id INT NOT NULL REFERENCES purchase_request_detail(purchase_request_detail_id),
    asset_type_id              INT NOT NULL REFERENCES asset_type(asset_type_id),
    quantity                   INT NOT NULL,
    quotation_detail_note      NVARCHAR(255) NULL,
    warranty_months            INT NULL,
    price                      INT NOT NULL,
    discount_rate              DECIMAL(5, 2) NOT NULL DEFAULT 0,
    tax_rate                   DECIMAL(5, 2) NOT NULL DEFAULT 0,
    reject_reason              NVARCHAR(255) NULL,
    status                     NVARCHAR(100) NULL, -- DRAFT, PENDING, APPROVED, REJECTED, DELETED
    spec_requirement           NVARCHAR(255) NULL
);

CREATE TABLE purchase_orders (
    purchase_order_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    total_amount        NUMERIC(19) NULL,
    note                NVARCHAR(255) NULL,
    status              NVARCHAR(40) NOT NULL, -- PENDING, COMPLETED, CANCELLED, DELETED
    created_at          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    purchase_request_id INT NOT NULL REFERENCES purchase_request(purchase_request_id),
    supplier_id         INT NOT NULL REFERENCES supplier(supplier_id),
    quotation_id        INT NOT NULL REFERENCES quotation(quotation_id),
    approved_by         INT NULL REFERENCES users(user_id),
    updated_at          DATETIME2(0) NULL,
    updated_by          INT NULL REFERENCES users(user_id)
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
    delivery_date          DATETIME2(0) NULL
);

-- =============================================
-- 4. TÀI SẢN (CORE ASSET)
-- =============================================

CREATE TABLE asset (
    asset_id                 INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    asset_name               NVARCHAR(100) NOT NULL,
    asset_type_id            INT NOT NULL REFERENCES asset_type(asset_type_id),
    purchase_order_detail_id INT NULL REFERENCES purchase_order_details(purchase_order_detail_id),
    current_status           NVARCHAR(40) NOT NULL, -- NEW, AVAILABLE, ASSIGNED, UNDER_MAINTENANCE, DISPOSED
    original_cost            NUMERIC(19, 2) NULL,
    department_id            INT NULL REFERENCES departments(department_id),
    acquisition_date         DATE NULL,
    in_service_date          DATE NULL,
    warranty_start_date      DATE NULL,
    warranty_end_date        DATE NULL
);

-- =============================================
-- 5. CẤP PHÁT (ALLOCATION)
-- =============================================

CREATE TABLE allocation_request (
    request_id              INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    requester_id            INT NOT NULL REFERENCES users(user_id),
    requested_department_id INT NOT NULL REFERENCES departments(department_id),
    request_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    needed_by_date          DATE NULL,
    priority                NVARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    reason                  NVARCHAR(500) NOT NULL,
    status                  NVARCHAR(40) NOT NULL, -- DRAFT, PENDING, APPROVED, ORDERED, REJECTED, DELETED
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

-- =============================================
-- 6. YÊU CẦU TRẢ (RETURN)
-- =============================================

CREATE TABLE return_request (
    request_id              INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    requester_id            INT NOT NULL REFERENCES users(user_id),
    requested_department_id INT NOT NULL REFERENCES departments(department_id),
    request_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    reason                  NVARCHAR(500) NOT NULL,
    status                  NVARCHAR(40) NOT NULL, -- DRAFT, PENDING, APPROVED, ORDERED, REJECTED, DELETED
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

-- =============================================
-- 7. LỆNH THỰC HIỆN (HANDOVER)
-- =============================================

CREATE TABLE asset_handover (
    handover_id             INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    handover_type           NVARCHAR(40) NOT NULL, -- ALLOCATION, RETURN, TRANSFER
    allocation_request_id   INT NULL REFERENCES allocation_request(request_id),
    return_request_id       INT NULL REFERENCES return_request(request_id),
    from_department_id      INT NULL REFERENCES departments(department_id),
    to_department_id        INT NULL REFERENCES departments(department_id),
    executed_by_user_id     INT NOT NULL REFERENCES users(user_id),
    handover_date           DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    status                  NVARCHAR(40) NOT NULL, -- PENDING, COMPLETE
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

-- =============================================
-- 8. ĐIỀU CHUYỂN (TRANSFER)
-- =============================================

CREATE TABLE transfer_request (
    transfer_id             INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    allocation_request_id   INT NULL REFERENCES allocation_request(request_id),
    from_department_id      INT NULL REFERENCES departments(department_id),
    to_department_id        INT NULL REFERENCES departments(department_id),
    asset_manager_id        INT NULL REFERENCES users(user_id),
    transfer_date           DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    reason                  NVARCHAR(500) NULL,
    status                  NVARCHAR(40) NOT NULL, -- DRAFT, PENDING, APPROVED, REJECTED, DELETED
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



CREATE INDEX idx_asset_department ON asset(department_id, asset_id);
CREATE INDEX idx_return_detail_asset ON return_request_detail(asset_id, request_id);
CREATE INDEX idx_return_request_status_id ON return_request(status, request_id);



CREATE TABLE qc_report (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    asset_id     INT NOT NULL,
    qc_status    NVARCHAR(40) NOT NULL, -- PASSED, FAILED, PENDING
    inspected_by INT NOT NULL,
    qc_date      DATETIME2 DEFAULT SYSDATETIME(),
    note         NVARCHAR(MAX),
    CONSTRAINT FK_qc_report_asset FOREIGN KEY (asset_id) REFERENCES asset(asset_id),
    CONSTRAINT FK_qc_report_inspected_by FOREIGN KEY (inspected_by) REFERENCES users(user_id)
);

-- =============================================
-- 9. MODULE KHO: SETUP KHÔNG GIAN LƯU TRỮ
-- =============================================

-- 9.1 Thông tin chung của Kho
CREATE TABLE wh_warehouses (
    warehouse_id    INT IDENTITY(1,1) PRIMARY KEY,
    name            NVARCHAR(100) NOT NULL,
    address         NVARCHAR(255) NOT NULL,
    manager_user_id INT NOT NULL REFERENCES users(user_id),
    status          NVARCHAR(40) DEFAULT N'ACTIVE', -- ACTIVE, INACTIVE
);

-- 9.2 Định mức thể tích/sức chứa của từng loại tài sản
CREATE TABLE wh_asset_capacity (
    asset_type_id   INT PRIMARY KEY REFERENCES asset_type(asset_type_id),
    unit_volume     INT NOT NULL DEFAULT 1 -- Ví dụ: Laptop = 1, Máy in = 3
);

-- 9.3 Quản lý Khu vực lưu trữ (Dynamic Zones)
CREATE TABLE wh_zones (
    zone_id          INT IDENTITY(1,1) PRIMARY KEY,
    warehouse_id     INT NOT NULL REFERENCES wh_warehouses(warehouse_id),
    zone_name        NVARCHAR(100) NOT NULL,
    max_capacity     INT NOT NULL,           -- Sức chứa tối đa của Zone
    current_capacity INT NOT NULL DEFAULT 0, -- Không gian đang bị chiếm dụng
    asset_type_id    INT NULL REFERENCES asset_type(asset_type_id), -- NULL = Zone đang trống hoàn toàn, sẵn sàng đón loại mới
    status           NVARCHAR(40) DEFAULT N'ACTIVE' -- ACTIVE, INACTIVE
);

-- =============================================
-- 10. MODULE KHO: QUẢN LÝ VỊ TRÍ HIỆN TẠI
-- =============================================

CREATE TABLE wh_asset_placement (
    asset_id        INT PRIMARY KEY REFERENCES asset(asset_id), -- Một tài sản chỉ nằm ở 1 chỗ
    zone_id         INT NOT NULL REFERENCES wh_zones(zone_id),
    placed_by       INT NOT NULL REFERENCES users(user_id),
    placed_at       DATETIME2(0) DEFAULT SYSDATETIME(),
    note            NVARCHAR(255) NULL
);

-- =============================================
-- 11. MODULE KHO: NHẬT KÝ GIAO DỊCH & ÁNH XẠ
-- =============================================

-- 11.1 Sổ cái giao dịch kho (Lõi)
CREATE TABLE wh_transactions (
    transaction_id   INT IDENTITY(1,1) PRIMARY KEY,
    asset_id         INT NOT NULL REFERENCES asset(asset_id),
    zone_id          INT NOT NULL REFERENCES wh_zones(zone_id), -- Zone đích (nhập) hoặc Zone nguồn (xuất)
    transaction_type NVARCHAR(20) NOT NULL, -- INBOUND, OUTBOUND
    executed_by      INT NOT NULL REFERENCES users(user_id),
    executed_at      DATETIME2(0) DEFAULT SYSDATETIME(),
    note             NVARCHAR(255) NULL
);

-- 11.2 Bảng trung gian: Ánh xạ Nhập kho từ PO Mua sắm (Khởi tạo tài sản mới)
CREATE TABLE map_po_transactions (
    purchase_order_id INT NOT NULL REFERENCES purchase_orders(purchase_order_id),
    transaction_id    INT NOT NULL REFERENCES wh_transactions(transaction_id),
    PRIMARY KEY (purchase_order_id, transaction_id)
);

-- 11.3 Bảng trung gian: Ánh xạ Nhập kho từ Lệnh Thu hồi (Luân chuyển về kho)
CREATE TABLE map_handover_transactions (
    asset_handover_id INT NOT NULL REFERENCES asset_handover(handover_id),
    transaction_id    INT NOT NULL REFERENCES wh_transactions(transaction_id),
    PRIMARY KEY (asset_handover_id, transaction_id)
);