-- =============================================
-- AssetManager - FULL SCRIPT (WITH IDEMPOTENT WAREHOUSE MODULE)
-- Tích hợp: Users, Departments, Purchase, Assets (Core) & Warehouse Context
-- SQL Server
-- =============================================

-- =============================================
-- 1. NHÓM TỔ CHỨC & NGƯỜI DÙNG
-- =============================================

-- 1.1 Departments (tạo trước, CHƯA gắn FK manager_user_id để tránh cycle)
CREATE TABLE departments (
    department_id   INT IDENTITY(1,1) NOT NULL,
    department_name NVARCHAR(150) NOT NULL,
    manager_user_id INT NULL,
    status          NVARCHAR(40) NOT NULL DEFAULT N'ACTIVE',
    created_date    DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date    DATETIME NULL,
    description     NVARCHAR(1000) NULL,
    PRIMARY KEY (department_id)
);

-- 1.2 Users
CREATE TABLE users (
    user_id        INT IDENTITY(1,1) NOT NULL,
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
    department_id  INT NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT FK_users_department
        FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

-- 1.3 Gắn FK manager_user_id sau khi users đã tồn tại
ALTER TABLE departments
    ADD CONSTRAINT FK_departments_manager_user
        FOREIGN KEY (manager_user_id) REFERENCES users(user_id);

CREATE UNIQUE INDEX UQ_departments_manager_user
    ON departments(manager_user_id)
    WHERE manager_user_id IS NOT NULL;


-- =============================================
-- 2. DANH MỤC CỐT LÕI (CATEGORIES & ASSET TYPES)
-- =============================================

CREATE TABLE category (
    category_id   INT IDENTITY(1,1) NOT NULL,
    category_name NVARCHAR(255) NOT NULL,
    description   NVARCHAR(255) NULL,
    status        NVARCHAR(40)  NOT NULL,
    PRIMARY KEY (category_id)
);

CREATE TABLE asset_type (
    asset_type_id               INT IDENTITY(1,1) NOT NULL,
    type_name                   NVARCHAR(255) NOT NULL,
    description                 NVARCHAR(255) NULL,
    type_class                  NVARCHAR(255) NOT NULL,
    status                      NVARCHAR(40)  NOT NULL,
    default_depreciation_method NVARCHAR(30)  NULL,
    default_useful_life_months  INT NULL,
    specification               NVARCHAR(255) NULL,
    category_id                 INT NOT NULL,
    model                       NVARCHAR(255) NULL,
    PRIMARY KEY (asset_type_id),
    CONSTRAINT FK_asset_type_category
        FOREIGN KEY (category_id) REFERENCES category(category_id)
);


-- =============================================
-- 3. QUY TRÌNH MUA SẮM (PURCHASING)
-- =============================================

CREATE TABLE supplier (
    supplier_id   INT IDENTITY(1,1) NOT NULL,
    supplier_name NVARCHAR(255) NOT NULL,
    phone_number  NVARCHAR(255) NOT NULL,
    email         NVARCHAR(255) NOT NULL,
    address       NVARCHAR(255) NOT NULL,
    supplier_code NVARCHAR(255) NULL,
    tax_code      NVARCHAR(255) NULL UNIQUE,
    status        NVARCHAR(255) NOT NULL,
    created_date  DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date  DATETIME NULL,
    PRIMARY KEY (supplier_id)
);

CREATE TABLE purchase_request (
    purchase_request_id      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    status                   NVARCHAR(40)  NOT NULL,
    request_reason           NVARCHAR(255) NULL,
    note                     NVARCHAR(255) NULL,
    creator_id               INT NOT NULL,
    requesting_department_id INT NULL,
    needed_by_date           DATETIME2(0) NULL,
    priority                 NVARCHAR(255) NOT NULL,
    approved_by_director_id  INT NULL,
    approved_by_director_at  DATETIME2(0) NULL,
    purchase_staff_user_id   INT NULL,
    reject_reason            NVARCHAR(255) NULL,
    created_at               DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at               DATETIME2(0) NULL,
    CONSTRAINT FK_purchase_request_creator FOREIGN KEY (creator_id) REFERENCES users(user_id),
    CONSTRAINT FK_purchase_request_dept FOREIGN KEY (requesting_department_id) REFERENCES departments(department_id),
    CONSTRAINT FK_purchase_request_director FOREIGN KEY (approved_by_director_id) REFERENCES users(user_id),
    CONSTRAINT FK_purchase_request_purchase_staff FOREIGN KEY (purchase_staff_user_id) REFERENCES users(user_id)
);

CREATE TABLE purchase_request_detail (
    purchase_request_detail_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    estimated_price            NUMERIC(19) NOT NULL,
    quantity                   INT NOT NULL,
    purchase_request_id        INT NOT NULL,
    asset_type_id              INT NOT NULL,
    spec_requirement           NVARCHAR(255) NULL,
    note                       NVARCHAR(255) NULL,
    CONSTRAINT FK_purchase_request_detail_purchase_request FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
    CONSTRAINT FK_purchase_request_detail_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id)
);

CREATE TABLE quotation (
    quotation_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    purchase_request_id INT NOT NULL,
    supplier_id         INT NOT NULL,
    quotation_date      DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    status              NVARCHAR(255) NOT NULL,
    total_amount        NUMERIC(19) NULL,
    created_at          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at          DATETIME2(0) NULL,
    reject_reason       NVARCHAR(255) NULL,
    CONSTRAINT FK_quotation_purchase_request FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
    CONSTRAINT FK_quotation_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id)
);

CREATE TABLE quotation_detail (
    quotation_detail_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    quotation_id               INT NOT NULL,
    purchase_request_detail_id INT NOT NULL,
    asset_type_id              INT NOT NULL,
    quantity                   INT NOT NULL,
    quotation_detail_note      NVARCHAR(255) NULL,
    warranty_months            INT NULL,
    price                      INT NOT NULL,
    discount_rate              DECIMAL(5, 2) NOT NULL DEFAULT 0,
    tax_rate                   DECIMAL(5, 2) NOT NULL DEFAULT 0,
    reject_reason              NVARCHAR(255) NULL,
    status                     NVARCHAR(100) NULL,
    spec_requirement           NVARCHAR(255) NULL,
    CONSTRAINT FK_quotation_detail_quotation FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id),
    CONSTRAINT FK_quotation_detail_purchase_request_detail FOREIGN KEY (purchase_request_detail_id) REFERENCES purchase_request_detail(purchase_request_detail_id),
    CONSTRAINT FK_quotation_detail_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id)
);

CREATE TABLE purchase_orders (
    purchase_order_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    order_date          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    total_amount        NUMERIC(19) NULL,
    note                NVARCHAR(255) NULL,
    status              NVARCHAR(40) NOT NULL,
    created_at          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    purchase_request_id INT NOT NULL,
    supplier_id         INT NOT NULL,
    quotation_id        INT NOT NULL,
    approved_by         INT NULL,
    updated_at          DATETIME2(0) NULL,
    updated_by          INT NULL,
    CONSTRAINT FK_purchase_orders_purchase_request FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
    CONSTRAINT FK_purchase_orders_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
    CONSTRAINT FK_purchase_orders_quotation FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id),
    CONSTRAINT FK_purchase_orders_approved_by FOREIGN KEY (approved_by) REFERENCES users(user_id),
    CONSTRAINT FK_purchase_orders_updated_by FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

CREATE TABLE purchase_order_details (
    purchase_order_detail_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    quantity               INT NOT NULL,
    unit_price             NUMERIC(19) NOT NULL,
    tax_rate               NUMERIC(5, 2) NULL,
    purchase_order_id      INT NOT NULL,
    asset_type_id          INT NOT NULL,
    discount               NUMERIC(19) NULL,
    note                   NVARCHAR(255) NULL,
    quotation_detail_id    INT NULL,
    expected_delivery_date DATETIME2(0) NULL,
    CONSTRAINT FK_purchase_order_details_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id),
    CONSTRAINT FK_purchase_order_details_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),
    CONSTRAINT FK_po_detail__quotation_detail FOREIGN KEY (quotation_detail_id) REFERENCES quotation_detail(quotation_detail_id)
);


-- =============================================
-- 4. TÀI SẢN (CORE ASSET)
-- =============================================
-- XÓA BỎ HOÀN TOÀN DẤU VẾT VỊ TRÍ, KHO BÃI
CREATE TABLE asset (
    asset_id            INT IDENTITY(1,1) NOT NULL,
    asset_name          NVARCHAR(100) NOT NULL,
    serial_number       NVARCHAR(100) NULL,
    asset_type_id       INT NOT NULL,
    current_status      NVARCHAR(40) NOT NULL, -- IN_USE, IN_WAREHOUSE, BROKEN, DISPOSED...
    original_cost       NUMERIC(19, 2) NULL,
    department_id       INT NULL, -- Nơi tài sản đang được sử dụng nếu đã xuất khỏi kho
    acquisition_date    DATE NULL,
    in_service_date     DATE NULL,
    warranty_start_date DATE NULL,
    warranty_end_date   DATE NULL,
    PRIMARY KEY (asset_id),
    CONSTRAINT FK_AST_Type FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),
    CONSTRAINT FK_AST_Department FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE UNIQUE INDEX UQ_asset_serial ON asset(serial_number) WHERE serial_number IS NOT NULL;


-- =============================================
-- 5. CẤP PHÁT (ALLOCATION)
-- =============================================

CREATE TABLE allocation_request (
    request_id              INT IDENTITY(1,1) NOT NULL,
    requester_id            INT NOT NULL,
    requested_department_id INT NOT NULL,
    request_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    needed_by_date          DATE NULL,
    priority                NVARCHAR(20) NOT NULL,
    reason                  NVARCHAR(500) NOT NULL,
    status                  NVARCHAR(40) NOT NULL,
    am_approved_by          INT NULL,
    am_approved_at          DATETIME2(0) NULL,
    reason_reject           NVARCHAR(255) NULL,
    created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at              DATETIME2(0) NULL,
    PRIMARY KEY (request_id),
    CONSTRAINT FK_alloc_req_requester FOREIGN KEY (requester_id) REFERENCES users(user_id),
    CONSTRAINT FK_alloc_req_dept FOREIGN KEY (requested_department_id) REFERENCES departments(department_id),
    CONSTRAINT FK_alloc_req_am FOREIGN KEY (am_approved_by) REFERENCES users(user_id)
);

CREATE TABLE allocation_request_detail (
    request_detail_id    INT IDENTITY(1,1) NOT NULL,
    request_id           INT NOT NULL,
    asset_type_id        INT NOT NULL,
    quantity_requested   INT NOT NULL,
    note                 NVARCHAR(255) NULL,
    PRIMARY KEY (request_detail_id),
    CONSTRAINT FK_req_details_parent FOREIGN KEY (request_id) REFERENCES allocation_request(request_id),
    CONSTRAINT FK_req_details_type FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id)
);

-- Phiếu cấp phát
CREATE TABLE allocation (
    allocation_id              INT IDENTITY(1,1) NOT NULL,
    allocation_request_id      INT NOT NULL,
    allocated_by_user_id       INT NOT NULL,
    allocated_to_department_id INT NOT NULL,
    allocation_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    status                     NVARCHAR(40) NOT NULL,
    note                       NVARCHAR(255) NULL,
    created_at                 DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_date               DATETIME2(0) NULL,
    PRIMARY KEY (allocation_id),
    CONSTRAINT FK_alloc_req FOREIGN KEY (allocation_request_id) REFERENCES allocation_request(request_id),
    CONSTRAINT FK_alloc_by FOREIGN KEY (allocated_by_user_id) REFERENCES users(user_id),
    CONSTRAINT FK_alloc_to FOREIGN KEY (allocated_to_department_id) REFERENCES departments(department_id)
);

CREATE TABLE allocation_detail (
    allocation_detail_id       INT IDENTITY(1,1) NOT NULL,
    allocation_id              INT NOT NULL,
    allocation_request_detail_id INT NOT NULL,
    asset_id                   INT NOT NULL,
    status                     NVARCHAR(40) NOT NULL,
    note                       NVARCHAR(255) NULL,
    PRIMARY KEY (allocation_detail_id),
    CONSTRAINT FK_alloc_det_alloc FOREIGN KEY (allocation_id) REFERENCES allocation(allocation_id),
    CONSTRAINT FK_alloc_det_req_det FOREIGN KEY (allocation_request_detail_id) REFERENCES allocation_request_detail(request_detail_id),
    CONSTRAINT FK_alloc_det_asset FOREIGN KEY (asset_id) REFERENCES asset(asset_id)
);


-- =========================================================================
-- =========================================================================
-- 6. MODULE KHO ĐỘC LẬP (WAREHOUSE CONTEXT)
-- =========================================================================
-- =========================================================================

-- Thông tin Kho hàng

CREATE TABLE wh_warehouses (

    id              INT IDENTITY(1,1) PRIMARY KEY,

    name            NVARCHAR(100) NOT NULL,

    address         NVARCHAR(255) NOT NULL,

    manager_user_id INT NOT NULL REFERENCES users(user_id),

    status          NVARCHAR(40) DEFAULT N'ACTIVE' --ACTIVE, INACTIVE

);



-- Vùng Không gian (Zone) - Điểm lưu trữ

CREATE TABLE wh_zones (

    id                     INT IDENTITY(1,1) PRIMARY KEY,

    warehouse_id           INT NOT NULL REFERENCES wh_warehouses(id),

    name                   NVARCHAR(100) NOT NULL,

    assigned_asset_type_id INT REFERENCES asset_type(id),

    max_capacity           INT NOT NULL,

    current_capacity       INT DEFAULT 0,

    status                 NVARCHAR(40) DEFAULT N'ACTIVE' --ACTIVE, INACTIVE

);



-- Thay vì thêm dung lượng vào master table asset_type, ta thiết kế bảng Mapping độc lập

CREATE TABLE wh_asset_capacity (

    id              INT IDENTITY(1,1) PRIMARY KEY,

    asset_type_id   INT NOT NULL UNIQUE REFERENCES asset_type(id),

    capacity_units  INT NOT NULL DEFAULT 1

);



-- Phiếu Kho (Vé Giao Dịch)

CREATE TABLE wh_inventory_ticket (

    id           INT IDENTITY(1,1) PRIMARY KEY,

    warehouse_id INT NOT NULL REFERENCES wh_warehouses(id),

    ticket_type  NVARCHAR(20) NOT NULL,

    status       NVARCHAR(40) DEFAULT N'INBOX', --INBOX, PENDING, COMPLETED, CANCELLED

    handle_by    INT NOT NULL REFERENCES users(user_id),

    created_at   DATETIME2 DEFAULT SYSDATETIME(),

    completed_at DATETIME2

);



-- Mục tiêu của phiếu

CREATE TABLE wh_ticket_detail (

    id            INT IDENTITY(1,1) PRIMARY KEY,

    ticket_id     INT NOT NULL REFERENCES wh_inventory_ticket(id),

    asset_type_id INT NOT NULL REFERENCES asset_type(id),

    quantity      INT NOT NULL,

    note          NVARCHAR(255)

);



-- Báo cáo kiểm định (Được đẩy lên trước bảng mapping)

CREATE TABLE qc_report (

    id           INT IDENTITY(1,1) PRIMARY KEY,

    asset_id     INT NOT NULL,

    qc_status    NVARCHAR(40) NOT NULL,

    inspected_by INT NOT NULL,

    qc_date      DATETIME2 DEFAULT SYSDATETIME(),

    note         NVARCHAR(MAX)

);



-- Cụ thể mapping

CREATE TABLE wh_ticket_asset_mapping (

    detail_id    INT NOT NULL REFERENCES wh_ticket_detail(id),

    asset_id     INT NOT NULL REFERENCES asset(id),

    qc_report_id INT REFERENCES qc_report(id),

    updated_at   DATETIME2 DEFAULT SYSDATETIME(),

    PRIMARY KEY (detail_id, asset_id)

);



-- Vị trí tài sản

CREATE TABLE wh_asset_location (

    asset_id       INT NOT NULL REFERENCES asset(id) PRIMARY KEY,

    zone_id        INT NOT NULL REFERENCES wh_zones(id),

    last_ticket_id INT NOT NULL REFERENCES wh_inventory_ticket(id)

);
