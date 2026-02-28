-- =============================================
-- AssetManager - FULL SCRIPT (REPLACED PROCUREMENT TABLES)
-- SQL Server
-- =============================================

--CREATE DATABASE AssetManager
--GO
--USE AssetManager
--GO

-- =============================================
-- 1. NHÓM TỔ CHỨC & NGƯỜI DÙNG
-- =============================================

/*
UPDATE users
SET role = 'ROLE_ADMIN'
WHERE user_id = 10;

SELECT * FROM departments
SELECT * FROM users u JOIN departments d ON u.department_id = d.department_id

SELECT 1 FROM Departments WHERE department_name = 'Information Technology Department'

UPDATE Users SET status = 'INACTIVE', updated_date = GETDATE() WHERE user_id = 7;

SELECT 1 FROM Users WHERE email = '0123456789'
*/

/*
INSERT INTO departments (department_name, manager_user_id, status, description)
VALUES
('IT Department', NULL, 'ACTIVE', 'Phòng Công nghệ thông tin'),
('HR Department', NULL, 'ACTIVE', 'Phòng Nhân sự'),
('Finance Department', NULL, 'ACTIVE', 'Phòng Tài chính - Kế toán'),
('Marketing Department', NULL, 'ACTIVE', 'Phòng Marketing'),
('Operations Department', NULL, 'ACTIVE', 'Phòng Vận hành'),
('Administration Department', NULL, 'ACTIVE', 'Phòng Hành chính');

INSERT INTO users
(username, password_hash, first_name, last_name, phone_number, email, status, role, department_id)
VALUES
('manager_it', '$2a$10$fakeHashForManager123', 'Nguyen', 'An', '0911111111', 'manager.it@example.com', 'ACTIVE', 'MANAGER', 1),
('staff_it1', '$2a$10$fakeHashForStaff123', 'Tran', 'Binh', '0922222222', 'staff1.it@example.com', 'ACTIVE', 'STAFF', 1),
('staff_hr1', '$2a$10$fakeHashForStaff123', 'Le', 'Chi', '0933333333', 'staff1.hr@example.com', 'ACTIVE', 'STAFF', 2),
('manager_hr', '$2a$10$fakeHashForManager123', 'Pham', 'Dung', '0944444444', 'manager.hr@example.com', 'ACTIVE', 'MANAGER', 2),
('staff_finance', '$2a$10$fakeHashForStaff123', 'Hoang', 'Mai', '0955555555', 'staff.finance@example.com', 'ACTIVE', 'STAFF', 3),
('manager_marketing', '$2a$10$fakeHashForManager123', 'Nguyen', 'Linh', '0966666666', 'manager.marketing@example.com', 'ACTIVE', 'MANAGER', 4),
('staff_marketing', '$2a$10$fakeHashForStaff123', 'Do', 'Thanh', '0977777777', 'staff.marketing@example.com', 'ACTIVE', 'STAFF', 4),
('staff_operations', '$2a$10$fakeHashForStaff123', 'Bui', 'Nam', '0988888888', 'staff.operations@example.com', 'ACTIVE', 'STAFF', 5),
('staff_admin', '$2a$10$fakeHashForStaff123', 'Vu', 'Hoa', '0999999999', 'staff.admin@example.com', 'ACTIVE', 'STAFF', 6);


===========================================================================================================
*/
SET NOCOUNT ON;

------------------------------------------------------------
-- 0) SEED tối thiểu để không vướng FK (IF NOT EXISTS)
------------------------------------------------------------

-- Departments
IF NOT EXISTS (SELECT 1 FROM departments WHERE department_name = N'IT Department')
    INSERT INTO departments (department_name, status, description) VALUES (N'IT Department', 'ACTIVE', N'Information Technology');

IF NOT EXISTS (SELECT 1 FROM departments WHERE department_name = N'Finance Department')
    INSERT INTO departments (department_name, status, description) VALUES (N'Finance Department', 'ACTIVE', N'Finance & Accounting');

IF NOT EXISTS (SELECT 1 FROM departments WHERE department_name = N'HR Department')
    INSERT INTO departments (department_name, status, description) VALUES (N'HR Department', 'ACTIVE', N'Human Resources');


DECLARE @deptIT INT = (SELECT TOP 1 department_id FROM departments WHERE department_name = N'IT Department' ORDER BY department_id);
DECLARE @deptFin INT = (SELECT TOP 1 department_id FROM departments WHERE department_name = N'Finance Department' ORDER BY department_id);
DECLARE @deptHR INT = (SELECT TOP 1 department_id FROM departments WHERE department_name = N'HR Department' ORDER BY department_id);


-- Users (director / manager / purchase staff)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'director01')
    INSERT INTO users (username, password_hash, first_name, last_name, email, status, role, department_id)
    VALUES (N'director01', N'hash_director', N'John', N'Director', N'director@company.com', N'ACTIVE', N'DIRECTOR', @deptIT);

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'it_manager')
    INSERT INTO users (username, password_hash, first_name, last_name, email, status, role, department_id)
    VALUES (N'it_manager', N'hash_manager', N'Alice', N'ITManager', N'it@company.com', N'ACTIVE', N'MANAGER', @deptIT);

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'purchase01')
    INSERT INTO users (username, password_hash, first_name, last_name, email, status, role, department_id)
    VALUES (N'purchase01', N'hash_purchase', N'Bob', N'Buyer', N'purchase@company.com', N'ACTIVE', N'PURCHASE_STAFF', @deptFin);


DECLARE @directorId INT = (SELECT TOP 1 user_id FROM users WHERE username = N'director01' ORDER BY user_id);
DECLARE @managerId  INT = (SELECT TOP 1 user_id FROM users WHERE username = N'it_manager' ORDER BY user_id);
DECLARE @purchaseStaffId INT = (SELECT TOP 1 user_id FROM users WHERE username = N'purchase01' ORDER BY user_id);

-- update manager_user_id (nếu đang NULL)
UPDATE departments SET manager_user_id = @managerId
WHERE department_id = @deptIT AND (manager_user_id IS NULL OR manager_user_id <> @managerId);


-- Category
IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = N'IT Equipment')
    INSERT INTO category (category_name, description, status)
    VALUES (N'IT Equipment', N'Computers, servers, peripherals', N'ACTIVE');

DECLARE @catIT INT = (SELECT TOP 1 category_id FROM category WHERE category_name = N'IT Equipment' ORDER BY category_id);

-- Asset Types (ít nhất 3 loại)
IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Laptop' AND category_id = @catIT)
    INSERT INTO asset_type (type_name, type_class, status, category_id, model)
    VALUES (N'Laptop', N'Hardware', N'ACTIVE', @catIT, N'Dell Latitude');

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Desktop PC' AND category_id = @catIT)
    INSERT INTO asset_type (type_name, type_class, status, category_id, model)
    VALUES (N'Desktop PC', N'Hardware', N'ACTIVE', @catIT, N'HP EliteDesk');

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Monitor' AND category_id = @catIT)
    INSERT INTO asset_type (type_name, type_class, status, category_id, model)
    VALUES (N'Monitor', N'Hardware', N'ACTIVE', @catIT, N'Dell 24"');

DECLARE @typeLaptop INT  = (SELECT TOP 1 asset_type_id FROM asset_type WHERE type_name = N'Laptop' ORDER BY asset_type_id);
DECLARE @typeDesktop INT = (SELECT TOP 1 asset_type_id FROM asset_type WHERE type_name = N'Desktop PC' ORDER BY asset_type_id);
DECLARE @typeMonitor INT = (SELECT TOP 1 asset_type_id FROM asset_type WHERE type_name = N'Monitor' ORDER BY asset_type_id);


-- Suppliers (ít nhất 3)
IF NOT EXISTS (SELECT 1 FROM supplier WHERE tax_code = N'TAX001')
    INSERT INTO supplier (supplier_name, phone_number, email, address, tax_code, status)
    VALUES (N'ABC Supplier', N'0909000001', N'abc@supplier.com', N'Hanoi', N'TAX001', N'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM supplier WHERE tax_code = N'TAX002')
    INSERT INTO supplier (supplier_name, phone_number, email, address, tax_code, status)
    VALUES (N'XYZ Supplier', N'0909000002', N'xyz@supplier.com', N'HCM City', N'TAX002', N'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM supplier WHERE tax_code = N'TAX003')
    INSERT INTO supplier (supplier_name, phone_number, email, address, tax_code, status)
    VALUES (N'Global Tech', N'0909000003', N'global@supplier.com', N'Da Nang', N'TAX003', N'ACTIVE');

DECLARE @sup1 INT = (SELECT TOP 1 supplier_id FROM supplier WHERE tax_code = N'TAX001' ORDER BY supplier_id);
DECLARE @sup2 INT = (SELECT TOP 1 supplier_id FROM supplier WHERE tax_code = N'TAX002' ORDER BY supplier_id);
DECLARE @sup3 INT = (SELECT TOP 1 supplier_id FROM supplier WHERE tax_code = N'TAX003' ORDER BY supplier_id);


------------------------------------------------------------
-- 1) INSERT 10 purchase_request (bắt ID bằng OUTPUT)
------------------------------------------------------------
DECLARE @pr TABLE (rn INT IDENTITY(1,1), purchase_request_id INT, status NVARCHAR(40), dept_id INT);

INSERT INTO purchase_request
(status, request_reason, note, creator_id, requesting_department_id, needed_by_date, priority,
 approved_by_director_id, approved_by_director_at, purchase_staff_user_id, reject_reason)
OUTPUT inserted.purchase_request_id, inserted.status, inserted.requesting_department_id
INTO @pr(purchase_request_id, status, dept_id)
VALUES
(N'DRAFT',     N'Buy laptops for new staff batch #1', N'Onboarding wave 1', @managerId, @deptIT,  '2026-03-05 00:00:00', N'HIGH',   NULL, NULL, @purchaseStaffId, NULL),
(N'SUBMITTED', N'Buy monitors for office expansion',  N'Need 24-inch monitors', @managerId, @deptIT, '2026-03-08 00:00:00', N'MEDIUM', NULL, NULL, @purchaseStaffId, NULL),
(N'APPROVED',  N'New desktops for IT lab',            N'Lab upgrade', @managerId, @deptIT,        '2026-03-01 00:00:00', N'HIGH',   @directorId, SYSDATETIME(), @purchaseStaffId, NULL),
(N'APPROVED',  N'Laptop replacement for seniors',     N'Replace old devices', @managerId, @deptIT,'2026-03-12 00:00:00', N'MEDIUM', @directorId, SYSDATETIME(), @purchaseStaffId, NULL),
(N'SUBMITTED', N'Monitor refresh for finance',        N'Accounting monitors', @purchaseStaffId, @deptFin,'2026-03-15 00:00:00', N'LOW', NULL, NULL, @purchaseStaffId, NULL),
(N'APPROVED',  N'Desktops for new interns',           N'Intern intake', @managerId, @deptIT,     '2026-03-20 00:00:00', N'LOW',    @directorId, SYSDATETIME(), @purchaseStaffId, NULL),
(N'DRAFT',     N'Extra laptops for backup pool',      N'Spare devices', @managerId, @deptIT,      '2026-03-25 00:00:00', N'LOW',    NULL, NULL, @purchaseStaffId, NULL),
(N'APPROVED',  N'Monitor + desktop combo for HR',     N'HR workstation', @purchaseStaffId, @deptHR,'2026-03-18 00:00:00', N'MEDIUM', @directorId, SYSDATETIME(), @purchaseStaffId, NULL),
(N'SUBMITTED', N'Laptop for Finance Manager',         N'High spec laptop', @purchaseStaffId, @deptFin,'2026-03-22 00:00:00', N'HIGH', NULL, NULL, @purchaseStaffId, NULL),
(N'APPROVED',  N'Replace broken desktops',            N'Urgent replacement', @managerId, @deptIT,'2026-03-02 00:00:00', N'HIGH',   @directorId, SYSDATETIME(), @purchaseStaffId, NULL);


------------------------------------------------------------
-- 2) INSERT 20 purchase_request_detail (2 dòng mỗi PR)
------------------------------------------------------------
DECLARE @prd TABLE (
  purchase_request_detail_id INT,
  purchase_request_id INT,
  asset_type_id INT,
  quantity INT
);

INSERT INTO purchase_request_detail
(estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement, note)
OUTPUT inserted.purchase_request_detail_id, inserted.purchase_request_id, inserted.asset_type_id, inserted.quantity
INTO @prd(purchase_request_detail_id, purchase_request_id, asset_type_id, quantity)
SELECT
    v.estimated_price,
    v.qty,
    pr.purchase_request_id,
    v.asset_type_id,
    v.spec_req,
    v.note
FROM @pr pr
CROSS APPLY (VALUES
   (CAST(25000000 AS NUMERIC(19)), 3, @typeLaptop,  N'RAM 16GB, SSD 512GB', N'Line 1'),
   (CAST( 5000000 AS NUMERIC(19)), 5, @typeMonitor, N'24-inch IPS',        N'Line 2')
) v(estimated_price, qty, asset_type_id, spec_req, note);


------------------------------------------------------------
-- 3) INSERT 12 quotation (2 quotation cho 6 PR đã APPROVED)
------------------------------------------------------------
DECLARE @q TABLE (quotation_id INT, purchase_request_id INT, supplier_id INT);

INSERT INTO quotation
(purchase_request_id, supplier_id, status, total_amount, reject_reason)
OUTPUT inserted.quotation_id, inserted.purchase_request_id, inserted.supplier_id
INTO @q(quotation_id, purchase_request_id, supplier_id)
SELECT TOP (12)
    pr.purchase_request_id,
    s.supplier_id,
    N'SUBMITTED',
    CAST(0 AS NUMERIC(19)),
    NULL
FROM @pr pr
CROSS JOIN (VALUES (@sup1), (@sup2)) s(supplier_id)
WHERE pr.status = N'APPROVED'
ORDER BY pr.purchase_request_id, s.supplier_id;


------------------------------------------------------------
-- 4) INSERT 20 quotation_detail (map theo detail của PR và quotation của PR)
--    Mỗi PR approved: lấy 2 detail của PR -> gán vào quotation của PR (ưu tiên supplier @sup2)
------------------------------------------------------------
DECLARE @qd TABLE (
  quotation_detail_id INT,
  quotation_id INT,
  purchase_request_detail_id INT,
  asset_type_id INT,
  quantity INT,
  price INT
);

;WITH QPick AS (
    SELECT q.quotation_id, q.purchase_request_id, q.supplier_id
    FROM @q q
),
PRDByPR AS (
    SELECT prd.purchase_request_id, prd.purchase_request_detail_id, prd.asset_type_id, prd.quantity,
           ROW_NUMBER() OVER (PARTITION BY prd.purchase_request_id ORDER BY prd.purchase_request_detail_id) AS rn
    FROM @prd prd
),
JoinData AS (
    SELECT
        q.quotation_id,
        prd.purchase_request_detail_id,
        prd.asset_type_id,
        prd.quantity,
        CASE
            WHEN prd.asset_type_id = @typeLaptop  THEN 24000000
            WHEN prd.asset_type_id = @typeDesktop THEN 17000000
            WHEN prd.asset_type_id = @typeMonitor THEN  4500000
            ELSE 1000000
        END AS price,
        ROW_NUMBER() OVER (ORDER BY q.quotation_id, prd.purchase_request_detail_id) AS seq
    FROM QPick q
    JOIN PRDByPR prd
      ON prd.purchase_request_id = q.purchase_request_id
    WHERE prd.rn IN (1,2) -- 2 detail mỗi PR
)
INSERT INTO quotation_detail
(quotation_id, purchase_request_detail_id, asset_type_id, quantity,
 quotation_detail_note, warranty_months, price, discount_rate, tax_rate, status, spec_requirement, reject_reason)
OUTPUT inserted.quotation_detail_id, inserted.quotation_id, inserted.purchase_request_detail_id,
       inserted.asset_type_id, inserted.quantity, inserted.price
INTO @qd(quotation_detail_id, quotation_id, purchase_request_detail_id, asset_type_id, quantity, price)
SELECT TOP (20)
    jd.quotation_id,
    jd.purchase_request_detail_id,
    jd.asset_type_id,
    jd.quantity,
    N'Quoted line item',
    CASE WHEN jd.asset_type_id IN (@typeLaptop, @typeDesktop) THEN 24 ELSE 12 END,
    jd.price,
    CAST(5.00 AS DECIMAL(5,2)),
    CAST(10.00 AS DECIMAL(5,2)),
    N'ACTIVE',
    N'As requested',
    NULL
FROM JoinData jd
ORDER BY jd.seq;


------------------------------------------------------------
-- 5) INSERT 6 purchase_orders (mỗi PR approved tạo 1 PO, chọn quotation của supplier @sup2)
------------------------------------------------------------
DECLARE @po TABLE (purchase_order_id INT, purchase_request_id INT, quotation_id INT, supplier_id INT);

INSERT INTO purchase_orders
(status, purchase_request_id, supplier_id, quotation_id, approved_by, updated_by)
OUTPUT inserted.purchase_order_id, inserted.purchase_request_id, inserted.quotation_id, inserted.supplier_id
INTO @po(purchase_order_id, purchase_request_id, quotation_id, supplier_id)
SELECT
    N'CREATED',
    pr.purchase_request_id,
    q.supplier_id,
    q.quotation_id,
    @directorId,
    NULL
FROM @pr pr
JOIN @q q
  ON q.purchase_request_id = pr.purchase_request_id
WHERE pr.status = N'APPROVED'
  AND q.supplier_id = @sup2;  -- chọn supplier 2 cho PO


------------------------------------------------------------
-- 6) INSERT 12 purchase_order_details (2 dòng mỗi PO, link quotation_detail_id cho khớp nghiệp vụ)
------------------------------------------------------------
INSERT INTO purchase_order_details
(purchase_order_id, asset_type_id, quantity, unit_price, tax_rate, discount, note, quotation_detail_id, expected_delivery_date)
SELECT
    po.purchase_order_id,
    qd.asset_type_id,
    qd.quantity,
    CAST(qd.price AS NUMERIC(19)),
    CAST(10.00 AS NUMERIC(5,2)),
    CAST(5.00  AS NUMERIC(19)),
    N'PO line item',
    qd.quotation_detail_id,
    DATEADD(DAY, 7, SYSDATETIME())
FROM @po po
JOIN @qd qd
  ON qd.quotation_id = po.quotation_id
WHERE qd.quotation_detail_id IN (
    SELECT TOP (12) quotation_detail_id
    FROM @qd
    WHERE quotation_id = po.quotation_id
    ORDER BY quotation_detail_id
);

------------------------------------------------------------
-- DONE - xem nhanh dữ liệu
------------------------------------------------------------
SELECT TOP 50 * FROM purchase_request ORDER BY purchase_request_id DESC;
SELECT TOP 50 * FROM purchase_request_detail ORDER BY purchase_request_detail_id DESC;
SELECT TOP 50 * FROM quotation ORDER BY quotation_id DESC;
SELECT TOP 50 * FROM quotation_detail ORDER BY quotation_detail_id DESC;
SELECT TOP 50 * FROM purchase_orders ORDER BY purchase_order_id DESC;
SELECT TOP 50 * FROM purchase_order_details ORDER BY purchase_order_detail_id DESC;



-- =============================================
-- AssetManager - FULL SCRIPT (ALL VARCHAR -> NVARCHAR)
-- SQL Server
-- =============================================

--CREATE DATABASE AssetManager;
--GO
--USE AssetManager;
--GO

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
-- 2. KHO VÀ DANH MỤC
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

CREATE TABLE warehouse (
                           warehouse_id       INT IDENTITY(1,1) NOT NULL,
                           warehouse_name     NVARCHAR(255) NOT NULL,
                           address            NVARCHAR(255) NULL,
                           status             NVARCHAR(40)  NOT NULL,
                           managed_by_user_id INT NOT NULL,
                           PRIMARY KEY (warehouse_id),
                           CONSTRAINT FK_warehouse_managed_by_user
                               FOREIGN KEY (managed_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE rack (
                      rack_id      INT IDENTITY(1,1) NOT NULL,
                      warehouse_id INT NOT NULL,
                      rack_name    NVARCHAR(255) NOT NULL,
                      description  NVARCHAR(255) NULL,
                      status       NVARCHAR(40)  NOT NULL,
                      created_date DATETIME NOT NULL DEFAULT GETDATE(),
                      updated_date DATETIME NULL,
                      PRIMARY KEY (rack_id),
                      CONSTRAINT FK_rack_warehouse
                          FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id)
);

CREATE TABLE shelf (
                       shelf_id         INT IDENTITY(1,1) NOT NULL,
                       shelf_name       NVARCHAR(255) NOT NULL,
                       current_capacity INT NOT NULL DEFAULT 0,
                       max_capacity     INT NOT NULL,
                       description      NVARCHAR(255) NULL,
                       rack_id          INT NOT NULL,
                       status           NVARCHAR(255) NULL,
                       created_date     DATETIME NOT NULL DEFAULT GETDATE(),
                       updated_date     DATETIME NULL,
                       PRIMARY KEY (shelf_id),
                       CONSTRAINT FK_shelf_rack
                           FOREIGN KEY (rack_id) REFERENCES rack(rack_id)
);


-- =============================================
-- 3. QUY TRÌNH MUA SẮM (AUTO-GEN)
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

                                  CONSTRAINT FK_purchase_request_creator
                                      FOREIGN KEY (creator_id) REFERENCES users(user_id),

                                  CONSTRAINT FK_purchase_request_dept
                                      FOREIGN KEY (requesting_department_id) REFERENCES departments(department_id),

                                  CONSTRAINT FK_purchase_request_director
                                      FOREIGN KEY (approved_by_director_id) REFERENCES users(user_id),

                                  CONSTRAINT FK_purchase_request_purchase_staff
                                      FOREIGN KEY (purchase_staff_user_id) REFERENCES users(user_id)
);

CREATE TABLE purchase_request_detail (
                                         purchase_request_detail_id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                                         estimated_price            NUMERIC(19) NOT NULL,
                                         quantity                   INT NOT NULL,

                                         purchase_request_id        INT NOT NULL,
                                         asset_type_id              INT NOT NULL,

                                         spec_requirement           NVARCHAR(255) NULL,
                                         note                       NVARCHAR(255) NULL,

                                         CONSTRAINT FK_purchase_request_detail_purchase_request
                                             FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),

                                         CONSTRAINT FK_purchase_request_detail_asset_type
                                             FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id)
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

                           CONSTRAINT FK_quotation_purchase_request
                               FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),

                           CONSTRAINT FK_quotation_supplier
                               FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id)
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

                                  CONSTRAINT FK_quotation_detail_quotation
                                      FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id),

                                  CONSTRAINT FK_quotation_detail_purchase_request_detail
                                      FOREIGN KEY (purchase_request_detail_id) REFERENCES purchase_request_detail(purchase_request_detail_id),

                                  CONSTRAINT FK_quotation_detail_asset_type
                                      FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id)
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

                                 CONSTRAINT FK_purchase_orders_purchase_request
                                     FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),

                                 CONSTRAINT FK_purchase_orders_supplier
                                     FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),

                                 CONSTRAINT FK_purchase_orders_quotation
                                     FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id),

                                 CONSTRAINT FK_purchase_orders_approved_by
                                     FOREIGN KEY (approved_by) REFERENCES users(user_id),

                                 CONSTRAINT FK_purchase_orders_updated_by
                                     FOREIGN KEY (updated_by) REFERENCES users(user_id)
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

                                        CONSTRAINT FK_purchase_order_details_purchase_order
                                            FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id),

                                        CONSTRAINT FK_purchase_order_details_asset_type
                                            FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),

                                        CONSTRAINT FK_po_detail__quotation_detail
                                            FOREIGN KEY (quotation_detail_id) REFERENCES quotation_detail(quotation_detail_id)
);


-- =============================================
-- 4. NHẬP KHO & TÀI SẢN
-- =============================================

CREATE TABLE goods_receipt (
                               goods_receipt_id     INT IDENTITY(1,1) NOT NULL,
                               purchase_order_id    INT NOT NULL,
                               warehouse_id         INT NOT NULL,
                               receipt_date         DATE NOT NULL,
                               received_by_user_id  INT NOT NULL,
                               inspected_by_user_id INT NOT NULL,
                               status               NVARCHAR(40) NOT NULL,
                               created_date         DATETIME NOT NULL DEFAULT GETDATE(),
                               updated_date         DATETIME NULL,
                               PRIMARY KEY (goods_receipt_id),

                               CONSTRAINT FK_GR_PO
                                   FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id),

                               CONSTRAINT FK_GR_WH
                                   FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id),

                               CONSTRAINT FK_GR_received_by
                                   FOREIGN KEY (received_by_user_id) REFERENCES users(user_id),

                               CONSTRAINT FK_GR_inspected_by
                                   FOREIGN KEY (inspected_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE asset (
                       asset_id            INT IDENTITY(1,1) NOT NULL,
                       serial_number       NVARCHAR(100) NULL UNIQUE,
                       asset_type_id       INT NOT NULL,
                       goods_receipt_id    INT NOT NULL,
                       current_status      NVARCHAR(40) NOT NULL,
                       original_cost       NUMERIC(19, 2) NULL,
                       shelf_id            INT NOT NULL,
                       warehouse_id        INT NULL,
                       department_id       INT NULL,
                       acquisition_date    DATE NULL,
                       in_service_date     DATE NULL,
                       warranty_start_date DATE NULL,
                       warranty_end_date   DATE NULL,
                       PRIMARY KEY (asset_id),

                       CONSTRAINT FK_AST_Type
                           FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),

                       CONSTRAINT FK_AST_GR
                           FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipt(goods_receipt_id),

                       CONSTRAINT FK_AST_Shelf
                           FOREIGN KEY (shelf_id) REFERENCES shelf(shelf_id),

                       CONSTRAINT FK_AST_Warehouse
                           FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id),

                       CONSTRAINT FK_AST_Department
                           FOREIGN KEY (department_id) REFERENCES departments(department_id)
);


-- =============================================
-- 5. NGHIỆP VỤ CẤP PHÁT (REFACTORED)
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

                                    CONSTRAINT FK_alloc_req_requester
                                        FOREIGN KEY (requester_id) REFERENCES users(user_id),

                                    CONSTRAINT FK_alloc_req_dept
                                        FOREIGN KEY (requested_department_id) REFERENCES departments(department_id),

                                    CONSTRAINT FK_alloc_req_am
                                        FOREIGN KEY (am_approved_by) REFERENCES users(user_id)
);

CREATE TABLE allocation_request_detail (
                                           request_detail_id    INT IDENTITY(1,1) NOT NULL,
                                           request_id           INT NOT NULL,
                                           asset_type_id        INT NOT NULL,
                                           quantity_requested   INT NOT NULL,
                                           note                 NVARCHAR(255) NULL,
                                           PRIMARY KEY (request_detail_id),

                                           CONSTRAINT FK_req_details_parent
                                               FOREIGN KEY (request_id) REFERENCES allocation_request(request_id),

                                           CONSTRAINT FK_req_details_type
                                               FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id)
);

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

                            CONSTRAINT FK_alloc_request
                                FOREIGN KEY (allocation_request_id) REFERENCES allocation_request(request_id),

                            CONSTRAINT FK_alloc_user
                                FOREIGN KEY (allocated_by_user_id) REFERENCES users(user_id),

                            CONSTRAINT FK_alloc_dept
                                FOREIGN KEY (allocated_to_department_id) REFERENCES departments(department_id)
);

CREATE TABLE allocation_detail (
                                   allocation_detail_id INT IDENTITY(1,1) NOT NULL,
                                   allocation_id        INT NOT NULL,
                                   asset_id             INT NOT NULL,
                                   assigned_to_user_id  INT NULL,
                                   issued_condition     NVARCHAR(255) NULL,
                                   PRIMARY KEY (allocation_detail_id),

                                   CONSTRAINT FK_detail_alloc_parent
                                       FOREIGN KEY (allocation_id) REFERENCES allocation(allocation_id),

                                   CONSTRAINT FK_detail_asset
                                       FOREIGN KEY (asset_id) REFERENCES asset(asset_id),

                                   CONSTRAINT FK_detail_user
                                       FOREIGN KEY (assigned_to_user_id) REFERENCES users(user_id)
);


-- =============================================
-- 6. LOG
-- =============================================

CREATE TABLE asset_log (
                           asset_log_id          INT IDENTITY(1,1) NOT NULL,
                           asset_id              INT NOT NULL,
                           action_type           NVARCHAR(155) NOT NULL,
                           from_department_id    INT NULL,
                           to_department_id      INT NULL,
                           action_date           DATETIME NOT NULL DEFAULT GETDATE(),
                           old_status            NVARCHAR(40) NULL,
                           related_allocation_id INT NULL,
                           note                  NVARCHAR(255) NULL,
                           PRIMARY KEY (asset_log_id),

                           CONSTRAINT FK_LOG_AST
                               FOREIGN KEY (asset_id) REFERENCES asset(asset_id),

                           CONSTRAINT FK_LOG_FROM_DEPT
                               FOREIGN KEY (from_department_id) REFERENCES departments(department_id),

                           CONSTRAINT FK_LOG_TO_DEPT
                               FOREIGN KEY (to_department_id) REFERENCES departments(department_id),

                           CONSTRAINT FK_LOG_ALLOCATION
                               FOREIGN KEY (related_allocation_id) REFERENCES allocation(allocation_id)
);