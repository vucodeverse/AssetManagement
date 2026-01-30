-- =============================================
--CREATE DATABASE AssetManager
-- =============================================

-- =============================================
-- 1. NHÓM TỔ CHỨC & NGƯỜI DÙNG
-- =============================================

CREATE TABLE departments (
  department_id   INT IDENTITY(1,1) NOT NULL,
  department_name VARCHAR(150) NOT NULL,
  manager_user_id INT NULL UNIQUE,
  status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
  created_date DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date DATETIME NULL,
  PRIMARY KEY (department_id)
);

CREATE TABLE users (
  user_id        INT IDENTITY(1,1) NOT NULL,
  username       VARCHAR(50)  NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  full_name      VARCHAR(150) NOT NULL,
  phone_number   VARCHAR(30)  NULL,
  email          VARCHAR(100) NULL UNIQUE,
  department_id  INT NOT NULL,
  status         VARCHAR(40)  NOT NULL,
  role           VARCHAR(40)  NOT NULL,
  created_date   DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date   DATETIME NULL,
  PRIMARY KEY (user_id),
  CONSTRAINT FK_users_department
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

ALTER TABLE departments
ADD CONSTRAINT FK_departments_manager_user
FOREIGN KEY (manager_user_id) REFERENCES users(user_id);

-- =============================================
-- 2. KHO VÀ DANH MỤC
-- =============================================

CREATE TABLE category (
  category_id   INT IDENTITY(1,1) NOT NULL,
  category_name VARCHAR(255) NOT NULL,
  description   VARCHAR(255) NULL,
  status        VARCHAR(40)  NOT NULL,
  PRIMARY KEY (category_id)
);

CREATE TABLE asset_type (
  asset_type_id               INT IDENTITY(1,1) NOT NULL,
  type_name                   NVARCHAR(255) NOT NULL,
  description                 VARCHAR(255) NULL,
  type_class                  VARCHAR(255) NOT NULL,
  status                      VARCHAR(40)  NOT NULL,
  default_depreciation_method VARCHAR(30)  NULL,
  default_useful_life_months  INT NULL,
  specification               VARCHAR(255) NULL,
  category_id                 INT NOT NULL,
  model                       VARCHAR(255) NULL,
  PRIMARY KEY (asset_type_id),
  CONSTRAINT FK_asset_type_category
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE warehouse (
  warehouse_id       INT IDENTITY(1,1) NOT NULL,
  warehouse_name     VARCHAR(255) NOT NULL,
  address            VARCHAR(255) NULL,
  status             VARCHAR(40)  NOT NULL,
  managed_by_user_id INT NOT NULL,
  PRIMARY KEY (warehouse_id),
  CONSTRAINT FK_warehouse_managed_by_user
    FOREIGN KEY (managed_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE rack (
  rack_id      INT IDENTITY(1,1) NOT NULL,
  warehouse_id INT NOT NULL,
  rack_name    VARCHAR(255) NOT NULL,
  description  VARCHAR(255) NULL,
  status       VARCHAR(40)  NOT NULL,
  created_date DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date DATETIME NULL,
  PRIMARY KEY (rack_id),
  CONSTRAINT FK_rack_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id)
);

CREATE TABLE shelf (
  shelf_id         INT IDENTITY(1,1) NOT NULL,
  shelf_name       VARCHAR(255) NOT NULL,
  current_capacity INT NOT NULL DEFAULT 0,
  max_capacity     INT NOT NULL,
  description      VARCHAR(255) NULL,
  rack_id          INT NOT NULL,
  status           VARCHAR(255) NULL,
  created_date     DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date     DATETIME NULL,
  PRIMARY KEY (shelf_id),
  CONSTRAINT FK_shelf_rack
    FOREIGN KEY (rack_id) REFERENCES rack(rack_id)
);

-- =============================================
-- 3. QUY TRÌNH MUA SẮM
-- =============================================

CREATE TABLE supplier (
  supplier_id   INT IDENTITY(1,1) NOT NULL,
  supplier_name VARCHAR(255) NOT NULL,
  phone_number  NVARCHAR(255) NOT NULL,
  email         VARCHAR(255) NOT NULL,
  address       VARCHAR(255) NOT NULL,
  supplier_code VARCHAR(255) NULL,
  tax_code      VARCHAR(255) NULL UNIQUE,
  status        VARCHAR(255) NOT NULL,
  created_date  DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date  DATETIME NULL,
  PRIMARY KEY (supplier_id)
);

CREATE TABLE purchase_request (
  purchase_request_id          INT IDENTITY(1,1) NOT NULL,
  status                       VARCHAR(40)  NOT NULL,
  note                         VARCHAR(255) NULL,
  created_by_user_id           INT NOT NULL,
  requesting_department_id     INT NOT NULL,
  needed_by_date               DATE NULL,
  priority                     VARCHAR(255) NOT NULL,
  approved_by_director_user_id INT NOT NULL,
  reject_reason                VARCHAR(255) NULL,
  created_date                 DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date                 DATETIME NULL,
  PRIMARY KEY (purchase_request_id),
  CONSTRAINT FK_PR_user
    FOREIGN KEY (created_by_user_id) REFERENCES users(user_id),
  CONSTRAINT FK_PR_dept
    FOREIGN KEY (requesting_department_id) REFERENCES departments(department_id)
);

CREATE TABLE purchase_request_detail (
  purchase_request_detail_id INT IDENTITY(1,1) NOT NULL,
  purchase_request_id        INT NOT NULL,
  asset_type_id              INT NOT NULL,
  quantity                   INT NOT NULL,
  specification_requirement  VARCHAR(255) NULL,
  note                       VARCHAR(255) NULL,
  PRIMARY KEY (purchase_request_detail_id),
  CONSTRAINT FK_PRD_PR
    FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
  CONSTRAINT FK_PRD_Type
    FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id)
);

CREATE TABLE quotation (
  quotation_id        INT IDENTITY(1,1) NOT NULL,
  purchase_request_id INT NOT NULL,
  supplier_id         INT NOT NULL,
  quotation_date      DATE NOT NULL,
  status              VARCHAR(255) NOT NULL,
  total_amount        NUMERIC(19, 2) NULL,
  note                VARCHAR(255) NULL,
  created_date        DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date        DATETIME NULL,
  PRIMARY KEY (quotation_id),
  CONSTRAINT FK_QUO_PR
    FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
  CONSTRAINT FK_QUO_SUP
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id)
);

CREATE TABLE purchase_order (
  purchase_order_id   INT IDENTITY(1,1) NOT NULL,
  purchase_request_id INT NOT NULL,
  supplier_id         INT NOT NULL,
  quotation_id        INT NOT NULL,
  order_date          DATE NOT NULL,
  total_amount        NUMERIC(19, 2) NULL,
  status              VARCHAR(40)  NOT NULL,
  approved_by_user_id INT NOT NULL,
  created_date        DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date        DATETIME NULL,
  updated_by_user_id  INT NULL,
  note                VARCHAR(255) NULL,
  PRIMARY KEY (purchase_order_id),
  CONSTRAINT FK_PO_PR
    FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
  CONSTRAINT FK_PO_SUP
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
  CONSTRAINT FK_PO_QUO
    FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id)
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
  status               VARCHAR(40) NOT NULL,
  created_date         DATETIME NOT NULL DEFAULT GETDATE(),
  updated_date         DATETIME NULL,
  PRIMARY KEY (goods_receipt_id),
  CONSTRAINT FK_GR_PO FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(purchase_order_id),
  CONSTRAINT FK_GR_WH FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id)
);

CREATE TABLE asset (
  asset_id            INT IDENTITY(1,1) NOT NULL,
  serial_number       VARCHAR(100) NULL UNIQUE,
  asset_type_id       INT NOT NULL,
  goods_receipt_id    INT NOT NULL,
  current_status      VARCHAR(40) NOT NULL,
  original_cost       NUMERIC(19, 2) NULL,
  shelf_id            INT NOT NULL,
  warehouse_id        INT NULL,
  department_id       INT NULL,
  acquisition_date    DATE NULL,
  in_service_date     DATE NULL,
  warranty_start_date DATE NULL,
  warranty_end_date   DATE NULL,
  PRIMARY KEY (asset_id),
  CONSTRAINT FK_AST_Type FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),
  CONSTRAINT FK_AST_GR FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipt(goods_receipt_id),
  CONSTRAINT FK_AST_Shelf FOREIGN KEY (shelf_id) REFERENCES shelf(shelf_id)
);

-- =============================================
-- 5. LOG
-- =============================================

CREATE TABLE asset_log (
  asset_log_id          INT IDENTITY(1,1) NOT NULL,
  asset_id              INT NOT NULL,
  action_type           VARCHAR(155) NOT NULL,
  from_department_id    INT NULL,
  to_department_id      INT NULL,
  action_date           DATETIME NOT NULL DEFAULT GETDATE(),
  old_status            VARCHAR(40) NULL,
  related_allocation_id INT NULL,
  note                  VARCHAR(255) NULL,
  PRIMARY KEY (asset_log_id),
  CONSTRAINT FK_LOG_AST FOREIGN KEY (asset_id) REFERENCES asset(asset_id)
);
