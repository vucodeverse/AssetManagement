CREATE DATABASE AssetManager;
GO
USE AssetManager;
GO

CREATE TABLE category (
                          category_id   INT IDENTITY(1,1) NOT NULL,
                          category_name NVARCHAR(255) NOT NULL,
                          description   NVARCHAR(255) NULL,
                          status        NVARCHAR(40)  NOT NULL,
                          PRIMARY KEY (category_id)
);

CREATE TABLE departments (
                             department_id   INT IDENTITY(1,1) NOT NULL,
                             department_name NVARCHAR(150) NOT NULL,
                             manager_user_id INT NULL,
                             status          NVARCHAR(40)  NOT NULL DEFAULT N'ACTIVE',
                             created_date    DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
                             updated_date    DATETIME2(0)  NULL,
                             description     NVARCHAR(1000) NULL,
                             PRIMARY KEY (department_id)
);

CREATE TABLE users (
                       user_id        INT IDENTITY(1,1) NOT NULL,
                       username       NVARCHAR(50)  NOT NULL,
                       password_hash  NVARCHAR(255) NOT NULL,
                       first_name     NVARCHAR(150) NOT NULL,
                       last_name      NVARCHAR(150) NOT NULL,
                       phone_number   NVARCHAR(30)  NULL,
                       email          NVARCHAR(100) NULL,
                       status         NVARCHAR(40)  NOT NULL,
                       role           NVARCHAR(40)  NOT NULL,
                       created_date   DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
                       updated_date   DATETIME2(0)  NULL,
                       department_id  INT NOT NULL,
                       PRIMARY KEY (user_id),
                       CONSTRAINT UQ_users_username UNIQUE (username),
                       CONSTRAINT UQ_users_email UNIQUE (email),
                       CONSTRAINT FK_users_department
                           FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

ALTER TABLE departments
    ADD CONSTRAINT FK_departments_manager_user
        FOREIGN KEY (manager_user_id) REFERENCES users(user_id);

CREATE UNIQUE INDEX UQ_departments_manager_user
    ON departments(manager_user_id)
    WHERE manager_user_id IS NOT NULL;

CREATE TABLE warehouse (
                           warehouse_id   INT IDENTITY(1,1) NOT NULL,
                           warehouse_name NVARCHAR(255) NOT NULL,
                           address        NVARCHAR(255) NULL,
                           status         NVARCHAR(40)  NOT NULL,
                           user_id        INT NOT NULL,
                           PRIMARY KEY (warehouse_id),
                           CONSTRAINT FK_warehouse_user
                               FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE rack (
                      rack_id      INT IDENTITY(1,1) NOT NULL,
                      warehouse_id INT NOT NULL,
                      rack_name    NVARCHAR(255) NOT NULL,
                      description  NVARCHAR(255) NULL,
                      status       NVARCHAR(40)  NOT NULL,
                      created_at   DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
                      updated_at   DATETIME2(0)  NULL,
                      PRIMARY KEY (rack_id),
                      CONSTRAINT FK_rack_warehouse
                          FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id)
);

CREATE TABLE shelves (
                         shelf_id         INT IDENTITY(1,1) NOT NULL,
                         shelf_name       NVARCHAR(255) NOT NULL,
                         current_capacity INT NOT NULL,
                         max_capacity     INT NOT NULL,
                         description      NVARCHAR(255) NULL,
                         rack_id          INT NOT NULL,
                         status           NVARCHAR(255) NULL,
                         created_at       DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                         updated_at       DATETIME2(0) NULL,
                         PRIMARY KEY (shelf_id),
                         CONSTRAINT FK_shelves_rack
                             FOREIGN KEY (rack_id) REFERENCES rack(rack_id)
);

CREATE TABLE supplier (
                          supplier_id   INT IDENTITY(1,1) NOT NULL,
                          supplier_name NVARCHAR(255) NOT NULL,
                          phone         NVARCHAR(30)  NOT NULL,
                          email         NVARCHAR(255) NOT NULL,
                          address       NVARCHAR(255) NOT NULL,
                          supplier_code NVARCHAR(255) NULL,
                          tax_code      NVARCHAR(255) NULL,
                          status        NVARCHAR(255) NOT NULL,
                          created_at    DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
                          updated_at    DATETIME2(0)  NULL,
                          PRIMARY KEY (supplier_id),
                          CONSTRAINT UQ_supplier_tax_code UNIQUE (tax_code)
);

CREATE TABLE asset_type (
                            asset_type_id                INT IDENTITY(1,1) NOT NULL,
                            asset_type_name              NVARCHAR(255) NOT NULL,
                            asset_type_class             NVARCHAR(255) NOT NULL,
                            status                       NVARCHAR(40)  NOT NULL,
                            default_depreciation_method  NVARCHAR(30)  NULL,
                            default_useful_life_months   INT NULL,
                            specification                NVARCHAR(255) NULL,
                            category_id                  INT NOT NULL,
                            model                        NVARCHAR(255) NULL,
                            PRIMARY KEY (asset_type_id),
                            CONSTRAINT FK_asset_type_category
                                FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE purchase_request (
                                  purchase_request_id         INT IDENTITY(1,1) NOT NULL,
                                  status                      NVARCHAR(40)  NOT NULL,
                                  request_reason              NVARCHAR(255) NOT NULL,
                                  note                        NVARCHAR(255) NULL,
                                  creator_id                  INT NOT NULL,
                                  requesting_department_id    INT NOT NULL,
                                  needed_by_date              DATETIME2(0)  NULL,
                                  priority                    NVARCHAR(255) NOT NULL,
                                  approved_by_director_id     INT NULL,
                                  approved_by_director_at     DATETIME2(0)  NULL,
                                  purchase_staff_user_id      INT NULL,
                                  reject_reason               NVARCHAR(255) NULL,
                                  created_at                  DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
                                  updated_at                  DATETIME2(0)  NULL,
                                  PRIMARY KEY (purchase_request_id),
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
                                         purchase_request_detail_id INT IDENTITY(1,1) NOT NULL,
                                         estimated_price            NUMERIC(19,0) NOT NULL,
                                         quantity                   INT NOT NULL,
                                         purchase_request_id        INT NOT NULL,
                                         asset_type_id              INT NOT NULL,
                                         spec_requirement           NVARCHAR(255) NULL,
                                         note                       NVARCHAR(255) NULL,
                                         PRIMARY KEY (purchase_request_detail_id),
                                         CONSTRAINT FK_purchase_request_detail_purchase_request
                                             FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
                                         CONSTRAINT FK_purchase_request_detail_asset_type
                                             FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),
                                         CONSTRAINT UQ_purchase_request_detail UNIQUE (purchase_request_id, asset_type_id)
);

CREATE TABLE quotation (
                           quotation_id        INT IDENTITY(1,1) NOT NULL,
                           purchase_request_id INT NOT NULL,
                           supplier_id         INT NOT NULL,
                           quotation_date      DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                           status              NVARCHAR(255) NOT NULL,
                           total_amount        NUMERIC(19,0) NULL,
                           note                NVARCHAR(255) NULL,
                           created_at          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                           updated_at          DATETIME2(0) NULL,
                           PRIMARY KEY (quotation_id),
                           CONSTRAINT FK_quotation_purchase_request
                               FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
                           CONSTRAINT FK_quotation_supplier
                               FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id)
);

CREATE TABLE quotation_detail (
                                  quotation_detail_id       INT IDENTITY(1,1) NOT NULL,
                                  quotation_id              INT NOT NULL,
                                  purchase_request_detail_id INT NOT NULL,
                                  asset_type_id             INT NOT NULL,
                                  quantity                  INT NOT NULL,
                                  note                      NVARCHAR(255) NULL,
                                  warranty_months           INT NULL,
                                  price                     INT NOT NULL ,
                                  PRIMARY KEY (quotation_detail_id),
                                  CONSTRAINT FK_quotation_detail_quotation
                                      FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id),
                                  CONSTRAINT FK_quotation_detail_purchase_request_detail
                                      FOREIGN KEY (purchase_request_detail_id) REFERENCES purchase_request_detail(purchase_request_detail_id),
                                  CONSTRAINT FK_quotation_detail_asset_type
                                      FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),
                                  CONSTRAINT UQ_quotation_detail UNIQUE (quotation_id, purchase_request_detail_id)
);

CREATE TABLE purchase_orders (
                                 purchase_order_id   INT IDENTITY(1,1) NOT NULL,
                                 order_date          DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                                 total_amount        NUMERIC(19,0) NULL,
                                 note                NVARCHAR(255) NULL,
                                 status              NVARCHAR(40)  NOT NULL,
                                 created_at          DATETIME2(0)  NOT NULL DEFAULT SYSDATETIME(),
                                 purchase_request_id INT NOT NULL,
                                 supplier_id         INT NOT NULL,
                                 quotation_id        INT NOT NULL,
                                 approved_by         INT NULL,
                                 updated_at          DATETIME2(0) NULL,
                                 updated_by          INT NULL,
                                 PRIMARY KEY (purchase_order_id),
                                 CONSTRAINT FK_purchase_orders_purchase_request
                                     FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id),
                                 CONSTRAINT FK_purchase_orders_supplier
                                     FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
                                 CONSTRAINT FK_purchase_orders_quotation
                                     FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id),
                                 CONSTRAINT FK_purchase_orders_approved_by
                                     FOREIGN KEY (approved_by) REFERENCES users(user_id),
                                 CONSTRAINT FK_purchase_orders_updated_by
                                     FOREIGN KEY (updated_by) REFERENCES users(user_id),
                                 CONSTRAINT UQ_purchase_orders_quotation UNIQUE (quotation_id)
);

CREATE TABLE purchase_order_details (
                                        purchase_order_detail_id INT IDENTITY(1,1) NOT NULL,
                                        quantity                 INT NOT NULL,
                                        unit_price               NUMERIC(19,0) NOT NULL,
                                        tax_rate                 NUMERIC(5,2) NULL,
                                        purchase_order_id        INT NOT NULL,
                                        asset_type_id            INT NOT NULL,
                                        discount                 NUMERIC(19,0) NULL,
                                        note                     NVARCHAR(255) NULL,
                                        PRIMARY KEY (purchase_order_detail_id),
                                        CONSTRAINT FK_purchase_order_details_purchase_order
                                            FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id),
                                        CONSTRAINT FK_purchase_order_details_asset_type
                                            FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),
                                        CONSTRAINT UQ_purchase_order_details UNIQUE (purchase_order_id, asset_type_id)
);

CREATE TABLE goods_receipt (
                               receipt_id       INT IDENTITY(1,1) NOT NULL,
                               receipt_date     DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                               purchase_order_id INT NOT NULL,
                               received_by      INT NOT NULL,
                               warehouse_id     INT NOT NULL,
                               inspected_by     INT NULL,
                               status           NVARCHAR(40) NOT NULL,
                               created_at       DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                               updated_at       DATETIME2(0) NULL,
                               PRIMARY KEY (receipt_id),
                               CONSTRAINT FK_goods_receipt_purchase_order
                                   FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id),
                               CONSTRAINT FK_goods_receipt_received_by
                                   FOREIGN KEY (received_by) REFERENCES users(user_id),
                               CONSTRAINT FK_goods_receipt_inspected_by
                                   FOREIGN KEY (inspected_by) REFERENCES users(user_id),
                               CONSTRAINT FK_goods_receipt_warehouse
                                   FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id)
);

CREATE TABLE goods_receipt_details (
                                       receipt_detail_id    INT IDENTITY(1,1) NOT NULL,
                                       receipt_id           INT NOT NULL,
                                       purchase_order_detail_id INT NOT NULL,
                                       asset_type_id        INT NOT NULL,
                                       received_quantity    INT NOT NULL,
                                       accepted_quantity    INT NOT NULL,
                                       rejected_quantity    INT NOT NULL,
                                       condition_on_arrival NVARCHAR(40) NOT NULL,
                                       unit_price           NUMERIC(19,0) NOT NULL,
                                       note                 NVARCHAR(255) NULL,
                                       PRIMARY KEY (receipt_detail_id),
                                       CONSTRAINT FK_goods_receipt_details_receipt
                                           FOREIGN KEY (receipt_id) REFERENCES goods_receipt(receipt_id),
                                       CONSTRAINT FK_goods_receipt_details_purchase_order_detail
                                           FOREIGN KEY (purchase_order_detail_id) REFERENCES purchase_order_details(purchase_order_detail_id),
                                       CONSTRAINT FK_goods_receipt_details_asset_type
                                           FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),
                                       CONSTRAINT UQ_goods_receipt_details UNIQUE (receipt_id, purchase_order_detail_id)
);

CREATE TABLE asset (
                       asset_id            INT IDENTITY(1,1) NOT NULL,
                       asset_name          NVARCHAR(255) NOT NULL,
                       serial_number       NVARCHAR(100) NULL,
                       current_status      NVARCHAR(40)  NOT NULL,
                       warranty_end_date   DATETIME2(0)  NULL,
                       initial_value       NUMERIC(19,0) NULL,
                       shelf_id            INT NOT NULL,
                       asset_type_id       INT NOT NULL,
                       receipt_request_id  INT NOT NULL,
                       acquisition_date    DATETIME2(0)  NULL,
                       in_service_date     DATETIME2(0)  NULL,
                       warranty_start_date DATETIME2(0)  NULL,
                       warehouse_id        INT NULL,
                       department_id       INT NULL,
                       PRIMARY KEY (asset_id),
                       CONSTRAINT UQ_asset_serial_number UNIQUE (serial_number),
                       CONSTRAINT FK_asset_warehouse
                           FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id),
                       CONSTRAINT FK_asset_shelf
                           FOREIGN KEY (shelf_id) REFERENCES shelves(shelf_id),
                       CONSTRAINT FK_asset_receipt
                           FOREIGN KEY (receipt_request_id) REFERENCES goods_receipt(receipt_id),
                       CONSTRAINT FK_asset_asset_type
                           FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id),
                       CONSTRAINT FK_asset_department
                           FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE allocation_request (
                                    request_id              INT IDENTITY(1,1) NOT NULL,
                                    request_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                                    status                  NVARCHAR(255) NOT NULL,
                                    reason                  NVARCHAR(255) NOT NULL,
                                    reason_reject           NVARCHAR(255) NULL,
                                    approve_by              INT NULL,
                                    requested_department_id INT NOT NULL,
                                    needed_by_date          DATETIME2(0) NULL,
                                    dm_approved_by          INT NULL,
                                    dm_approved_at          DATETIME2(0) NULL,
                                    am_approved_by          INT NULL,
                                    am_approved_at          DATETIME2(0) NULL,
                                    created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                                    updated_at              DATETIME2(0) NULL,
                                    PRIMARY KEY (request_id),
                                    CONSTRAINT FK_allocation_request_department
                                        FOREIGN KEY (requested_department_id) REFERENCES departments(department_id),
                                    CONSTRAINT FK_allocation_request_approve_by
                                        FOREIGN KEY (approve_by) REFERENCES users(user_id),
                                    CONSTRAINT FK_allocation_request_dm_approved_by
                                        FOREIGN KEY (dm_approved_by) REFERENCES users(user_id),
                                    CONSTRAINT FK_allocation_request_am_approved_by
                                        FOREIGN KEY (am_approved_by) REFERENCES users(user_id)
);

CREATE TABLE allocation_request_details (
                                            allocation_detail_id INT IDENTITY(1,1) NOT NULL,
                                            allocation_id        INT NOT NULL,
                                            asset_id             INT NOT NULL,
                                            issued_condition     NVARCHAR(40) NOT NULL,
                                            unit_value           NUMERIC(19,0) NOT NULL,
                                            note                 NVARCHAR(255) NULL,
                                            PRIMARY KEY (allocation_detail_id),
                                            CONSTRAINT FK_allocation_request_details_request
                                                FOREIGN KEY (allocation_id) REFERENCES allocation_request(request_id),
                                            CONSTRAINT FK_allocation_request_details_asset
                                                FOREIGN KEY (asset_id) REFERENCES asset(asset_id),
                                            CONSTRAINT UQ_allocation_request_details UNIQUE (allocation_id, asset_id)
);

CREATE TABLE allocation (
                            allocation_id   INT IDENTITY(1,1) NOT NULL,
                            allocation_by   INT NOT NULL,
                            allocation_to   INT NOT NULL,
                            allocation_from INT NOT NULL,
                            allocation_date DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                            status          NVARCHAR(40) NOT NULL,
                            note            NVARCHAR(255) NULL,
                            created_at      DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                            request_id      INT NOT NULL,
                            sign_off_at     DATETIME2(0) NULL,
                            created_by      INT NOT NULL,
                            updated_at      DATETIME2(0) NULL,
                            PRIMARY KEY (allocation_id),
                            CONSTRAINT FK_allocation_request
                                FOREIGN KEY (request_id) REFERENCES allocation_request(request_id),
                            CONSTRAINT FK_allocation_allocation_by
                                FOREIGN KEY (allocation_by) REFERENCES users(user_id),
                            CONSTRAINT FK_allocation_created_by
                                FOREIGN KEY (created_by) REFERENCES users(user_id),
                            CONSTRAINT FK_allocation_to_dept
                                FOREIGN KEY (allocation_to) REFERENCES departments(department_id),
                            CONSTRAINT FK_allocation_from_dept
                                FOREIGN KEY (allocation_from) REFERENCES departments(department_id)
);

CREATE TABLE allocation_detail (
                                   allocation_detail_id INT IDENTITY(1,1) NOT NULL,
                                   allocation_id        INT NOT NULL,
                                   asset_id             INT NOT NULL,
                                   issued_condition     NVARCHAR(255) NULL,
                                   unit_value           DECIMAL(19,0) NULL,
                                   note                 NVARCHAR(255) NULL,
                                   PRIMARY KEY (allocation_detail_id),
                                   CONSTRAINT FK_allocation_detail_allocation
                                       FOREIGN KEY (allocation_id) REFERENCES allocation(allocation_id),
                                   CONSTRAINT FK_allocation_detail_asset
                                       FOREIGN KEY (asset_id) REFERENCES asset(asset_id),
                                   CONSTRAINT UQ_allocation_detail UNIQUE (allocation_id, asset_id)
);

CREATE TABLE stock_issue (
                             stock_issue_id        INT IDENTITY(1,1) NOT NULL,
                             warehouse_id          INT NOT NULL,
                             related_allocation_id INT NOT NULL,
                             status                NVARCHAR(40) NOT NULL,
                             issued_by             INT NOT NULL,
                             created_at            DATETIME2(0) NULL,
                             updated_at            DATETIME2(0) NULL,
                             PRIMARY KEY (stock_issue_id),
                             CONSTRAINT FK_stock_issue_warehouse
                                 FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id),
                             CONSTRAINT FK_stock_issue_allocation
                                 FOREIGN KEY (related_allocation_id) REFERENCES allocation(allocation_id),
                             CONSTRAINT FK_stock_issue_issued_by
                                 FOREIGN KEY (issued_by) REFERENCES users(user_id)
);

CREATE TABLE stock_issue_detail (
                                    stock_issue_detail_id INT IDENTITY(1,1) NOT NULL,
                                    stock_issue_id        INT NOT NULL,
                                    asset_id              INT NOT NULL,
                                    quantity              INT NOT NULL,
                                    note                  NVARCHAR(255) NULL,
                                    PRIMARY KEY (stock_issue_detail_id),
                                    CONSTRAINT FK_stock_issue_detail_issue
                                        FOREIGN KEY (stock_issue_id) REFERENCES stock_issue(stock_issue_id),
                                    CONSTRAINT FK_stock_issue_detail_asset
                                        FOREIGN KEY (asset_id) REFERENCES asset(asset_id),
                                    CONSTRAINT UQ_stock_issue_detail UNIQUE (stock_issue_id, asset_id)
);

CREATE TABLE asset_users (
                             asset_users_id        INT IDENTITY(1,1) NOT NULL,
                             status                NVARCHAR(40) NOT NULL,
                             note                  NVARCHAR(255) NULL,
                             asset_id              INT NOT NULL,
                             assigned_to_user_id   INT NOT NULL,
                             assigned_by_user_id   INT NOT NULL,
                             assigned_at           DATETIME2(0) NULL,
                             related_allocation_id INT NULL,
                             unassigned_at         DATETIME2(0) NULL,
                             PRIMARY KEY (asset_users_id),
                             CONSTRAINT FK_asset_users_asset
                                 FOREIGN KEY (asset_id) REFERENCES asset(asset_id),
                             CONSTRAINT FK_asset_users_assigned_to
                                 FOREIGN KEY (assigned_to_user_id) REFERENCES users(user_id),
                             CONSTRAINT FK_asset_users_assigned_by
                                 FOREIGN KEY (assigned_by_user_id) REFERENCES users(user_id),
                             CONSTRAINT FK_asset_users_allocation
                                 FOREIGN KEY (related_allocation_id) REFERENCES allocation(allocation_id)
);

CREATE TABLE asset_logs (
                            log_id                INT IDENTITY(1,1) NOT NULL,
                            action_type           NVARCHAR(155) NOT NULL,
                            from_department       INT NULL,
                            to_department         INT NULL,
                            note                  NVARCHAR(255) NULL,
                            action_date           DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                            asset_id              INT NOT NULL,
                            old_status            NVARCHAR(40) NULL,
                            created_at            DATETIME2(0) NULL,
                            related_allocation_id INT NULL,
                            PRIMARY KEY (log_id),
                            CONSTRAINT FK_asset_logs_asset
                                FOREIGN KEY (asset_id) REFERENCES asset(asset_id),
                            CONSTRAINT FK_asset_logs_from_dept
                                FOREIGN KEY (from_department) REFERENCES departments(department_id),
                            CONSTRAINT FK_asset_logs_to_dept
                                FOREIGN KEY (to_department) REFERENCES departments(department_id),
                            CONSTRAINT FK_asset_logs_allocation
                                FOREIGN KEY (related_allocation_id) REFERENCES allocation(allocation_id)
);

CREATE TABLE revocation (
                            revocation_id              INT IDENTITY(1,1) NOT NULL,
                            revocation_date            DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                            status                     NVARCHAR(40) NOT NULL,
                            reason                     NVARCHAR(255) NULL,
                            note                       NVARCHAR(255) NULL,
                            created_at                 DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                            updated_at                 DATETIME2(0) NULL,
                            revoked_from_department_id INT NOT NULL,
                            created_by                 INT NOT NULL,
                            PRIMARY KEY (revocation_id),
                            CONSTRAINT FK_revocation_dept
                                FOREIGN KEY (revoked_from_department_id) REFERENCES departments(department_id),
                            CONSTRAINT FK_revocation_created_by
                                FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE revocation_detail (
                                   revocation_detail_id INT IDENTITY(1,1) NOT NULL,
                                   revocation_id        INT NOT NULL,
                                   asset_id             INT NOT NULL,
                                   returned_condition   NVARCHAR(255) NOT NULL,
                                   returned_quantity    INT NOT NULL,
                                   damage_level         NVARCHAR(255) NULL,
                                   note                 NVARCHAR(255) NULL,
                                   PRIMARY KEY (revocation_detail_id),
                                   CONSTRAINT FK_revocation_detail_revocation
                                       FOREIGN KEY (revocation_id) REFERENCES revocation(revocation_id),
                                   CONSTRAINT FK_revocation_detail_asset
                                       FOREIGN KEY (asset_id) REFERENCES asset(asset_id),
                                   CONSTRAINT UQ_revocation_detail UNIQUE (revocation_id, asset_id)
);
