USE master
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'AssetManager')
BEGIN
    DROP DATABASE AssetManager;
END

CREATE DATABASE AssetManager;

GO
USE AssetManager;



-- =============================================
-- 1. DANH MỤC CỐT LÕI (CATEGORIES & ASSET TYPES)
-- =============================================

CREATE TABLE category (
    category_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    category_name NVARCHAR(255) NOT NULL,
    description   NVARCHAR(255) NULL,
    status        NVARCHAR(40)  NOT NULL -- Enum: Status (DRAFT, PENDING, APPROVED, REJECTED, DELETED)
);

CREATE TABLE asset_type (
    asset_type_id               INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    type_name                   NVARCHAR(255) NOT NULL,
    description                 NVARCHAR(255) NULL,
    type_class                  NVARCHAR(255) NOT NULL, -- Enum: AssetTypeClass (FIXED_ASSET, TOOL, EQUIPMENT, CONSUMABLE, HARDWARE, SOFTWARE, ELECTRONICS, FURNITURE, IT_ASSET, OFFICE_ASSET)
    status                      NVARCHAR(40)  NOT NULL, -- Enum: Status (DRAFT, PENDING, APPROVED, REJECTED, DELETED)
    default_depreciation_method NVARCHAR(30)  NULL, -- Enum: DepreciationMethod (STRAIGHT_LINE, DECLINING_BALANCE)
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
    manager_user_id INT NULL, -- Foreign Key to users(user_id)
    status          NVARCHAR(40) NOT NULL DEFAULT N'ACTIVE', -- Enum: Status/UserStatus
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
    status         NVARCHAR(40)  NOT NULL, -- Enum: Status/UserStatus
    role           NVARCHAR(40)  NOT NULL, -- Enum: Role (ADMIN, PURCHASE_STAFF, ASSET_MANAGER, DEPARTMENT_MANAGER, WAREHOUSE_STAFF, DIRECTOR)
    created_date   DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date   DATETIME NULL,
    department_id  INT NULL REFERENCES departments(department_id)
);

-- Thêm ràng buộc vòng giữa departments và users
ALTER TABLE departments
    ADD CONSTRAINT FK_departments_manager FOREIGN KEY (manager_user_id) REFERENCES users(user_id);

CREATE UNIQUE INDEX UQ_departments_manager_user
    ON departments(manager_user_id)
    WHERE manager_user_id IS NOT NULL;

-- =============================================
-- 3. NHÀ CUNG CẤP (SUPPLIER)
-- =============================================

CREATE TABLE supplier (
    supplier_id   INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    supplier_name NVARCHAR(255) NOT NULL,
    phone_number  NVARCHAR(255) NOT NULL,
    email         NVARCHAR(255) NOT NULL,
    address       NVARCHAR(255) NOT NULL,
    supplier_code NVARCHAR(255) NOT NULL UNIQUE,
    tax_code      NVARCHAR(255) NULL UNIQUE,
    status        NVARCHAR(255) NOT NULL, -- Enum: SupplierStatus (ACTIVE, INACTIVE)
    created_date  DATETIME NOT NULL DEFAULT GETDATE(),
    updated_date  DATETIME NULL
);

-- =============================================
-- 4. QUY TRÌNH MUA SẮM (PURCHASING)
-- =============================================

CREATE TABLE purchase_request (
    purchase_request_id      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    status                   NVARCHAR(40)  NOT NULL, -- Enum: PurchaseProcessStatus (DRAFT, PENDING, APPROVED, ORDERED, REJECTED, DELETED, COMPLETED, CANCELLED, PARTIALLY_RECEIVED)
    request_reason           NVARCHAR(255) NOT NULL, -- Java: Purchase.reason
    note                     NVARCHAR(255) NULL,     -- Java: Purchase.purchaseNote
    creator_id               INT NOT NULL REFERENCES users(user_id), -- Java: Purchase.createdByUser
    needed_by_date           DATETIME2(0) NOT NULL,
    priority                 NVARCHAR(255) NOT NULL, -- Enum: Priority (LOW, MEDIUM, HIGH, CRITICAL)
    approved_by_director_id  INT NULL REFERENCES users(user_id), -- Java: Purchase.approvedByDirector
    approved_by_director_at  DATETIME2(0) NULL, -- Java: Purchase.approvedAt
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
    status              NVARCHAR(255) NOT NULL, -- Enum: Status (DRAFT, PENDING, APPROVED, REJECTED, DELETED)
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
    status              NVARCHAR(40) NOT NULL, -- Enum: PurchaseProcessStatus
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
    received_quantity      INT NOT NULL DEFAULT 0,
    created_at             DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at             DATETIME2(0) NULL
);

-- =============================================
-- 5. TÀI SẢN (CORE ASSET)
-- =============================================

CREATE TABLE asset (
    asset_id                 INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    asset_name               NVARCHAR(100) NOT NULL,
    asset_type_id            INT NOT NULL REFERENCES asset_type(asset_type_id),
    purchase_order_detail_id INT NULL REFERENCES purchase_order_details(purchase_order_detail_id),
    -- NOTE: Java model 'Asset' includes 'voucherDetailId' which is missing here.
    current_status           NVARCHAR(40) NOT NULL, -- Enum: AssetStatus (NEW, AVAILABLE, ALLOCATED, ASSIGNED, UNDER_MAINTENANCE, DISPOSED)
    original_cost            NUMERIC(19, 2) NULL,
    department_id            INT NULL REFERENCES departments(department_id),
    acquisition_date         DATE NULL,
    in_service_date          DATE NULL,
    warranty_start_date      DATE NULL,
    warranty_end_date        DATE NULL
);

CREATE INDEX idx_asset_department ON asset(department_id, asset_id);

-- =============================================
-- 6. CẤP PHÁT & THU HỒI (ALLOCATION & RETURN)
-- =============================================

CREATE TABLE allocation_request (
    request_id              INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    requester_id            INT NOT NULL REFERENCES users(user_id),
    requested_department_id INT NOT NULL REFERENCES departments(department_id),
    request_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    needed_by_date          DATE NULL,
    priority                NVARCHAR(20) NOT NULL, -- Enum: Priority (LOW, MEDIUM, HIGH, CRITICAL)
    reason                  NVARCHAR(500) NOT NULL, -- Java: AllocationRequest.requestReason
    status                  NVARCHAR(40) NOT NULL, -- Enum: Status (DRAFT, PENDING, APPROVED, REJECTED, DELETED, PENDING_AM, PENDING_DIRECTOR, IN_PROGRESS, COMPLETED, SUBMITTED)
    am_approved_by          INT NULL REFERENCES users(user_id), -- Java: AllocationRequest.assetManagerApprovedByUserId
    am_approved_at          DATETIME2(0) NULL, -- Java: AllocationRequest.assetManagerApprovedDate
    reason_reject           NVARCHAR(255) NULL, -- Java: AllocationRequest.rejectReason
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

CREATE TABLE return_request (
    request_id              INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    requester_id            INT NOT NULL REFERENCES users(user_id),
    requested_department_id INT NOT NULL REFERENCES departments(department_id),
    request_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    reason                  NVARCHAR(500) NOT NULL,
    status                  NVARCHAR(40) NOT NULL, -- Enum: Status (DRAFT, PENDING, APPROVED, REJECTED, DELETED, PENDING_AM)
    wh_confirmed_by         INT NULL REFERENCES users(user_id),
    wh_confirmed_at         DATETIME2(0) NULL,
    created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at              DATETIME2(0) NULL
);

CREATE TABLE return_request_detail (
    request_detail_id    INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    request_id           INT NOT NULL REFERENCES return_request(request_id),
    asset_id			 INT NOT NULL REFERENCES asset(asset_id),
    note                 NVARCHAR(255) NULL
);

CREATE INDEX idx_return_detail_asset ON return_request_detail(asset_id, request_id);
CREATE INDEX idx_return_request_status_id ON return_request(status, request_id);

-- =============================================
-- 7. ĐIỀU CHUYỂN (TRANSFER)
-- =============================================

CREATE TABLE transfer_request (
    transfer_id             INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    allocation_request_id   INT NULL REFERENCES allocation_request(request_id),
    from_department_id      INT NULL REFERENCES departments(department_id),
    to_department_id        INT NULL REFERENCES departments(department_id),
    asset_manager_id        INT NULL REFERENCES users(user_id),
    transfer_date           DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    reason                  NVARCHAR(500) NULL,
    status                  NVARCHAR(40) NOT NULL, -- Enum: TransferStatus (PENDING, SENDER_CONFIRMED, WAREHOUSE_CONFIRMED, COMPLETED, CANCELLED)
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

ALTER TABLE transfer_request_detail
    ADD CONSTRAINT fk_transfer_detail_transfer
    FOREIGN KEY (transfer_id)
    REFERENCES transfer_request(transfer_id)
    ON DELETE CASCADE;

-- =============================================
-- 8. KIỂM ĐỊNH & BÀN GIAO (QC & HANDOVER)
-- =============================================

CREATE TABLE qc_report (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    asset_id     INT NOT NULL REFERENCES asset(asset_id),
    qc_status    NVARCHAR(40) NOT NULL,
    inspected_by INT NOT NULL REFERENCES users(user_id),
    qc_date      DATETIME2 DEFAULT SYSDATETIME(),
    note         NVARCHAR(MAX),
    source_type  NVARCHAR(40) NULL,
    source_id    INT NULL
);

CREATE TABLE asset_handover (
    handover_id             INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    handover_type           NVARCHAR(40) NOT NULL, -- Enum: HandoverType (ALLOCATION, RETURN)
    allocation_request_id   INT NULL REFERENCES allocation_request(request_id),
    return_request_id       INT NULL REFERENCES return_request(request_id),
    from_department_id      INT NULL REFERENCES departments(department_id),
    to_department_id        INT NULL REFERENCES departments(department_id),
    status                  NVARCHAR(40) NOT NULL, -- Enum: Status (DRAFT, PENDING, APPROVED, REJECTED, DELETED)
    created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    updated_at              DATETIME2(0) NULL
);

CREATE TABLE asset_handover_detail (
    handover_detail_id      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    handover_id             INT NOT NULL REFERENCES asset_handover(handover_id),
    asset_id                INT NOT NULL REFERENCES asset(asset_id),
    qc_report_id            INT NULL REFERENCES qc_report(id),
    note                    NVARCHAR(255) NULL
);

-- =============================================
-- 9. MODULE KHO (WAREHOUSE MODULE)
-- =============================================

-- NOTE: Code (WhTransactionDAOImpl) references 'wh_inventory_vouchers' and 'wh_inventory_voucher_details' which are missing from this schema.
-- NOTE: Table 'wh_receipts' below may be deprecated or redundant given the Java models.

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
--Phieu xuat/nhap kho
CREATE TABLE wh_receipts (
    receipt_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    receipt_no        NVARCHAR(50) NOT NULL UNIQUE, 
    purchase_order_id INT NULL REFERENCES purchase_orders(purchase_order_id),
    asset_handover_id INT NULL REFERENCES asset_handover(handover_id),
    receipt_type      NVARCHAR(20) NOT NULL, -- Enum: 'INBOUND_PO', 'INBOUND_RETURN', 'OUTBOUND_ALLOCATION'
    created_at        DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    created_by        INT NOT NULL REFERENCES users(user_id),
    note              NVARCHAR(500) NULL
);

CREATE TABLE wh_transactions (
    transaction_id   INT IDENTITY(1,1) PRIMARY KEY,
    receipt_id		  INT NULL REFERENCES wh_receipts(receipt_id),
    asset_id         INT NOT NULL REFERENCES asset(asset_id),
    zone_id          INT NOT NULL REFERENCES wh_zones(zone_id), 
    transaction_type NVARCHAR(20) NOT NULL, -- Enum: 'INBOUND', 'OUTBOUND'
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

-- =============================================
-- 10. NHẬT KÝ TÀI SẢN (ASSET LOGS)
-- =============================================

CREATE TABLE asset_logs (
    asset_log_id        INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    asset_id            INT NOT NULL,
    action_type         NVARCHAR(50) NOT NULL, -- Enum: AssetActionType (CREATE, ALLOCATE, TRANSFER, RETURN, STATUS_CHANGE, DISPOSE)
    from_department_id  INT NULL,
    to_department_id    INT NULL,
    action_date         DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    old_status          NVARCHAR(40) NULL, -- Enum: AssetStatus
    new_status          NVARCHAR(40) NULL, -- Enum: AssetStatus
    related_allocation_id INT NULL,
    related_transfer_id   INT NULL,
    related_return_id     INT NULL,
    note                NVARCHAR(500) NULL,
    CONSTRAINT FK_asset_logs_asset FOREIGN KEY (asset_id) REFERENCES asset(asset_id),
    CONSTRAINT FK_asset_logs_from_dept FOREIGN KEY (from_department_id) REFERENCES departments(department_id),
    CONSTRAINT FK_asset_logs_to_dept FOREIGN KEY (to_department_id) REFERENCES departments(department_id),
    CONSTRAINT FK_asset_logs_allocation FOREIGN KEY (related_allocation_id) REFERENCES allocation_request(request_id),
    CONSTRAINT FK_asset_logs_transfer FOREIGN KEY (related_transfer_id) REFERENCES transfer_request(transfer_id),
    CONSTRAINT FK_asset_logs_return FOREIGN KEY (related_return_id) REFERENCES return_request(request_id)
);

CREATE INDEX idx_asset_logs_asset ON asset_logs(asset_id);
CREATE INDEX idx_asset_logs_action_date ON asset_logs(action_date);


-- =============================================
-- 11. DỮ LIỆU MẪU (SỬA LẠI ĐỂ CÓ PO PENDING)
-- =============================================

-- 1. Categories
INSERT INTO category (category_name, description, status) VALUES 
(N'IT Assets', N'Thiết bị công nghệ thông tin', N'ACTIVE'),
(N'Office Assets', N'Thiết bị văn phòng', N'ACTIVE'),
(N'Furniture', N'Nội thất văn phòng', N'ACTIVE');

-- 2. Asset Types
INSERT INTO asset_type (type_name, description, type_class, status, category_id, model) VALUES 
(N'Laptop Dell XPS', N'Máy tính xách tay cao cấp', 'HARDWARE', 'ACTIVE', 1, 'XPS 15 2024'),
(N'Monitor LG 27 inch', N'Màn hình đồ họa', 'HARDWARE', 'ACTIVE', 1, '27UL850'),
(N'Bàn làm việc gỗ', N'Bàn nhân viên 1m2', 'FURNITURE', 'ACTIVE', 3, 'OF-TABLE-01');

-- 3. Departments
INSERT INTO departments (department_name, status, description) VALUES 
(N'Board of Directors', 'ACTIVE', N'Ban Giám đốc'),
(N'IT Department', 'ACTIVE', N'Phòng Công nghệ thông tin'),
(N'Warehouse', 'ACTIVE', N'Phòng Kho vận'),
(N'Purchasing', 'ACTIVE', N'Phòng Mua sắm');

-- 4. Users
INSERT INTO users (username, password_hash, first_name, last_name, email, status, role, department_id) VALUES 
('admin', 'hash_123', N'Admin', N'System', 'admin@fpt.edu.vn', 'ACTIVE', 'ADMIN', 2),
('director', 'hash_123', N'Văn A', N'Nguyễn', 'director@fpt.edu.vn', 'ACTIVE', 'DIRECTOR', 1),
('asmanager', 'hash_123', N'Thị B', N'Trần', 'am@fpt.edu.vn', 'ACTIVE', 'ASSET_MANAGER', 4),
('whstaff', 'hash_123', N'Văn C', N'Lê', 'warehouse@fpt.edu.vn', 'ACTIVE', 'WAREHOUSE_STAFF', 3),
('purstaff', 'hash_123', N'Thị D', N'Hoàng', 'purchase@fpt.edu.vn', 'ACTIVE', 'PURCHASE_STAFF', 4);

-- 5. Suppliers
INSERT INTO supplier (supplier_name, phone_number, email, address, supplier_code, tax_code, status) VALUES 
(N'Phong Vũ IT', '0281234567', 'contact@phongvu.vn', N'TP. Hồ Chí Minh', 'SUP-PV01', 'MST001', 'ACTIVE'),
(N'Hòa Phát Furniture', '0249876543', 'sale@hoaphat.com', N'Hà Nội', 'SUP-HP01', 'MST002', 'ACTIVE');

-- 6. Purchase Requests
INSERT INTO purchase_request (status, request_reason, creator_id, needed_by_date, priority) VALUES 
(N'ORDERED', N'Bổ sung thiết bị IT tháng 4', 1, '2026-04-10', N'HIGH'),
(N'PENDING', N'Setup văn phòng mới', 5, '2026-05-01', N'MEDIUM'),
(N'PENDING', N'Dự phòng thiết bị kho', 4, '2026-04-15', N'MEDIUM');

-- 7. Purchase Request Details
INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement) VALUES 
(45000000, 8, 1, 1, N'RAM 32GB'), 
(25000000, 10, 2, 3, N'Gỗ sồi'),
(45000000, 2, 3, 1, N'RAM 16GB');

-- 8. Quotations
INSERT INTO quotation (purchase_request_id, supplier_id, status, total_amount) VALUES 
(1, 1, N'APPROVED', 360000000),
(2, 2, N'PENDING', 250000000),
(3, 1, N'PENDING', 90000000);

-- 9. Quotation Details
INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price) VALUES 
(1, 1, 1, 8, 45000000),
(2, 2, 3, 10, 25000000),
(3, 3, 1, 2, 45000000);

-- 10. Purchase Orders (1 ORDERED, 2 PENDING)
INSERT INTO purchase_orders (total_amount, note, status, purchase_request_id, supplier_id, quotation_id, approved_by) 
VALUES (360000000, N'Đơn hàng IT đã duyệt', N'ORDERED', 1, 1, 1, 2);

INSERT INTO purchase_order_details (quantity, unit_price, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (8, 45000000, 1, 1, 1, '2026-04-15');

INSERT INTO purchase_orders (total_amount, note, status, purchase_request_id, supplier_id, quotation_id, approved_by) 
VALUES (250000000, N'Đơn nội thất chờ sếp duyệt', N'PENDING', 2, 2, 2, NULL);

INSERT INTO purchase_order_details (quantity, unit_price, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (10, 25000000, 2, 3, 2, '2026-05-15');

INSERT INTO purchase_orders (total_amount, note, status, purchase_request_id, supplier_id, quotation_id, approved_by) 
VALUES (90000000, N'Đơn dự phòng chờ sếp duyệt', N'PENDING', 3, 1, 3, NULL);

INSERT INTO purchase_order_details (quantity, unit_price, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (2, 45000000, 3, 1, 3, '2026-04-20');

-- 11. Warehouse & Zones
INSERT INTO wh_warehouses (name, address, manager_user_id) VALUES (N'Kho Tổng Hà Nội', N'Hòa Lạc', 4);
INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, asset_type_id) VALUES (1, N'Khu A - Laptop', 100, 1);

-- ======================================================================
-- 13. BỔ SUNG TÀI SẢN VÀO KHO & TẠO LỆNH CẤP PHÁT (ĐÃ FIX LỖI IDENTITY)
-- ======================================================================

-- ---------------------------------------------------------
-- BƯỚC 1: Bổ sung Màn hình (Type 2) và Bàn (Type 3) vào kho (Dept 3)
-- ---------------------------------------------------------
-- Tạo thêm Zone cho Màn hình (Zone 2) và Bàn (Zone 3)
INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, asset_type_id, current_capacity) 
VALUES 
(1, N'Khu B - Màn hình', 50, 2, 5),
(1, N'Khu C - Nội thất', 20, 3, 3);

-- Insert 5 Màn hình LG
INSERT INTO asset (asset_name, asset_type_id, current_status, department_id, acquisition_date)
VALUES 
(N'Màn hình LG 27 inch - MON001', 2, N'AVAILABLE', 3, '2026-03-20'),
(N'Màn hình LG 27 inch - MON002', 2, N'AVAILABLE', 3, '2026-03-20'),
(N'Màn hình LG 27 inch - MON003', 2, N'AVAILABLE', 3, '2026-03-20'),
(N'Màn hình LG 27 inch - MON004', 2, N'AVAILABLE', 3, '2026-03-20'),
(N'Màn hình LG 27 inch - MON005', 2, N'AVAILABLE', 3, '2026-03-20');

INSERT INTO asset (asset_name, asset_type_id, current_status, department_id, acquisition_date)
VALUES 
(N'Laptop Dell XPS - IT006', 1, N'AVAILABLE', 3, SYSDATETIME()),
(N'Laptop Dell XPS - IT007', 1, N'AVAILABLE', 3, SYSDATETIME()),
(N'Laptop Dell XPS - IT008', 1, N'AVAILABLE', 3, SYSDATETIME()),
(N'Laptop Dell XPS - IT009', 1, N'AVAILABLE', 3, SYSDATETIME()),
(N'Laptop Dell XPS - IT010', 1, N'AVAILABLE', 3, SYSDATETIME());

-- 2. Đưa 5 máy này vào Zone 1 (Khu A - Laptop) do nhân viên kho (User 4) thực hiện
INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, note)
SELECT asset_id, 1, 4, N'Tồn kho bổ sung test luồng xuất' 
FROM asset 
WHERE asset_name IN (
    N'Laptop Dell XPS - IT006',
    N'Laptop Dell XPS - IT007',
    N'Laptop Dell XPS - IT008',
    N'Laptop Dell XPS - IT009',
    N'Laptop Dell XPS - IT010'
);

-- Dùng SELECT để lấy động ID của màn hình vừa insert đưa vào Zone 2
INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, note)
SELECT asset_id, 2, 4, N'Tồn kho' 
FROM asset 
WHERE asset_name LIKE N'Màn hình LG 27 inch - MON%';

-- Insert 3 Bàn làm việc
INSERT INTO asset (asset_name, asset_type_id, current_status, department_id, acquisition_date)
VALUES 
(N'Bàn làm việc gỗ - DESK001', 3, N'AVAILABLE', 3, '2026-03-20'),
(N'Bàn làm việc gỗ - DESK002', 3, N'AVAILABLE', 3, '2026-03-20'),
(N'Bàn làm việc gỗ - DESK003', 3, N'AVAILABLE', 3, '2026-03-20');

-- Dùng SELECT để lấy động ID của bàn làm việc vừa insert đưa vào Zone 3
INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, note)
SELECT asset_id, 3, 4, N'Tồn kho' 
FROM asset 
WHERE asset_name LIKE N'Bàn làm việc gỗ - DESK%';


-- ---------------------------------------------------------
-- BƯỚC 2: Tạo Yêu cầu cấp phát (APPROVED) & Lệnh bàn giao (PENDING)
-- ---------------------------------------------------------

-- Yêu cầu 1: Đa tài sản (1 Laptop, 2 Màn hình, 1 Bàn)
INSERT INTO allocation_request (requester_id, requested_department_id, needed_by_date, priority, reason, status, am_approved_by, am_approved_at)
VALUES 
(1, 2, '2026-04-05', N'MEDIUM', N'Setup chỗ ngồi làm việc cho Dev mới', N'APPROVED', 3, SYSDATETIME());

DECLARE @AllocReqId_1 INT = SCOPE_IDENTITY();

INSERT INTO allocation_request_detail (request_id, asset_type_id, quantity_requested, note)
VALUES 
(@AllocReqId_1, 1, 1, N'1 Laptop Dell XPS'),
(@AllocReqId_1, 2, 2, N'2 Màn hình LG 27 inch'),
(@AllocReqId_1, 3, 1, N'1 Bàn làm việc');

INSERT INTO asset_handover (handover_type, allocation_request_id, from_department_id, to_department_id, status)
VALUES 
(N'ALLOCATION', @AllocReqId_1, 3, 2, N'PENDING');


-- Yêu cầu 2: Đa tài sản (1 Laptop, 1 Màn hình)
INSERT INTO allocation_request (requester_id, requested_department_id, needed_by_date, priority, reason, status, am_approved_by, am_approved_at)
VALUES 
(2, 1, '2026-04-10', N'HIGH', N'Nâng cấp thiết bị cho Giám đốc', N'APPROVED', 3, SYSDATETIME());

DECLARE @AllocReqId_2 INT = SCOPE_IDENTITY();

INSERT INTO allocation_request_detail (request_id, asset_type_id, quantity_requested, note)
VALUES 
(@AllocReqId_2, 1, 1, N'Laptop dòng mới nhất'),
(@AllocReqId_2, 2, 1, N'Màn hình phụ');

INSERT INTO asset_handover (handover_type, allocation_request_id, from_department_id, to_department_id, status)
VALUES 
(N'ALLOCATION', @AllocReqId_2, 3, 1, N'PENDING');

-- ======================================================================
-- BỔ SUNG DỮ LIỆU MẪU: LUỒNG THU HỒI TÀI SẢN (RETURN TO WAREHOUSE)
-- ======================================================================

-- ---------------------------------------------------------
-- 1. Tạo Category và Asset Type mới (hoặc dùng lại để test)
-- ---------------------------------------------------------
DECLARE @CatPrinterId INT;
DECLARE @TypePrinterId INT;

-- Thêm Category: Thiết bị in ấn
INSERT INTO category (category_name, description, status) 
VALUES (N'Thiết bị in ấn', N'Máy in, máy scan các loại', N'ACTIVE');
SET @CatPrinterId = SCOPE_IDENTITY();

-- Thêm Asset Type: Máy in HP
INSERT INTO asset_type (type_name, description, type_class, status, category_id, model) 
VALUES (N'Máy in HP LaserJet', N'Máy in trắng đen cho văn phòng', 'HARDWARE', 'ACTIVE', @CatPrinterId, 'HP 107w');
SET @TypePrinterId = SCOPE_IDENTITY();


-- ---------------------------------------------------------
-- 2. Tạo Tài sản (Asset) đang được sử dụng tại Phòng ban
-- Giả sử Phòng IT (department_id = 2) đang giữ các tài sản này
-- ---------------------------------------------------------
DECLARE @AssetPrinter1 INT;
DECLARE @AssetLaptop1 INT;

-- Tạo 1 Máy in đang gán cho Phòng IT
INSERT INTO asset (asset_name, asset_type_id, current_status, department_id, acquisition_date)
VALUES (N'Máy in HP LaserJet - PRN-IT-01', @TypePrinterId, N'ASSIGNED', 2, '2025-01-10');
SET @AssetPrinter1 = SCOPE_IDENTITY();

-- Lấy loại tài sản Laptop Dell XPS (asset_type_id = 1 từ script gốc) để tạo 1 Laptop cũ
INSERT INTO asset (asset_name, asset_type_id, current_status, department_id, acquisition_date)
VALUES (N'Laptop Dell XPS Cũ - IT-OLD-99', 1, N'ASSIGNED', 2, '2024-06-15');
SET @AssetLaptop1 = SCOPE_IDENTITY();


-- ---------------------------------------------------------
-- 3. Tạo Yêu cầu thu hồi (Return Request & Detail)
-- Phòng IT (User 1) yêu cầu trả lại 2 tài sản trên về kho
-- ---------------------------------------------------------
DECLARE @ReturnReqId INT;

-- Đơn yêu cầu trả tài sản đã được duyệt (APPROVED) để sẵn sàng bàn giao
INSERT INTO return_request (requester_id, requested_department_id, reason, status)
VALUES (1, 2, N'Tài sản hỏng (máy in) và nhân sự nghỉ việc (laptop)', N'APPROVED');
SET @ReturnReqId = SCOPE_IDENTITY();

-- Chi tiết yêu cầu thu hồi (Map với 2 tài sản vừa tạo)
INSERT INTO return_request_detail (request_id, asset_id, note)
VALUES 
(@ReturnReqId, @AssetPrinter1, N'Máy in kẹt giấy liên tục, cần bảo hành hoặc thanh lý'),
(@ReturnReqId, @AssetLaptop1, N'Thu hồi do nhân sự IT nghỉ việc');


-- ---------------------------------------------------------
-- 4. Tạo Lệnh bàn giao thu hồi (Asset Handover & Detail)
-- Lệnh này đổ về cho nhân viên kho (Department 3) xử lý nhập kho
-- ---------------------------------------------------------
DECLARE @ReturnHandoverId INT;

-- Tạo biên bản bàn giao (Status: PENDING chờ kho xác nhận)
INSERT INTO asset_handover (handover_type, return_request_id, from_department_id, to_department_id, status)
VALUES (N'RETURN', @ReturnReqId, 2, 3, N'PENDING');
SET @ReturnHandoverId = SCOPE_IDENTITY();

-- Chi tiết bàn giao thu hồi
INSERT INTO asset_handover_detail (handover_id, asset_id, note)
VALUES 
(@ReturnHandoverId, @AssetPrinter1, N'Bàn giao máy in HP (trạng thái hỏng)'),
(@ReturnHandoverId, @AssetLaptop1, N'Bàn giao Laptop Dell (trạng thái hoạt động bình thường)');

GO