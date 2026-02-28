CREATE DATABASE AssetManager;
GO
USE AssetManager;
GO

CREATE TABLE category (
                          category_id INT IDENTITY PRIMARY KEY,
                          category_name NVARCHAR(255) NOT NULL,
                          description NVARCHAR(255),
                          status NVARCHAR(40) NOT NULL
);

CREATE TABLE users (
                       user_id INT IDENTITY PRIMARY KEY,
                       username NVARCHAR(50) NOT NULL UNIQUE,
                       password_hash NVARCHAR(255) NOT NULL,
                       fullname NVARCHAR(150) NOT NULL,
                       phone INT NULL,
                       email NVARCHAR(100) NULL UNIQUE,
                       created_at DATETIME2(0) NOT NULL,
                       status NVARCHAR(40) NOT NULL,
                       role NVARCHAR(40) NOT NULL,
                       updated_at DATETIME2(0) NULL
);

CREATE TABLE departments (
                             department_id INT IDENTITY PRIMARY KEY,
                             department_name NVARCHAR(150) NOT NULL,
                             manager_id INT NOT NULL UNIQUE
);

CREATE TABLE supplier (
                          supplier_id INT IDENTITY PRIMARY KEY,
                          supplier_name NVARCHAR(255) NOT NULL,
                          phone NVARCHAR(255) NOT NULL,
                          email NVARCHAR(255) NOT NULL,
                          address NVARCHAR(255) NOT NULL,
                          supplier_code NVARCHAR(255),
                          tax_code NVARCHAR(255) UNIQUE,
                          status NVARCHAR(255) NOT NULL,
                          created_at DATETIME2(0) NOT NULL,
                          updated_at DATETIME2(0)
);

CREATE TABLE warehouse (
                           warehouse_id INT IDENTITY PRIMARY KEY,
                           warehouse_name NVARCHAR(255) NOT NULL,
                           address NVARCHAR(255),
                           status NVARCHAR(40) NOT NULL,
                           user_id INT NOT NULL
);

CREATE TABLE rack (
                      rack_id INT IDENTITY PRIMARY KEY,
                      warehouse_id INT NOT NULL,
                      rack_name NVARCHAR(255) NOT NULL,
                      description NVARCHAR(255),
                      status NVARCHAR(40) NOT NULL,
                      created_at DATETIME2(0) NOT NULL,
                      updated_at DATETIME2(0)
);

CREATE TABLE shelves (
                         shelf_id INT IDENTITY PRIMARY KEY,
                         shelf_name NVARCHAR(255) NOT NULL,
                         current_capacity INT NOT NULL,
                         max_capacity INT NOT NULL,
                         description NVARCHAR(255),
                         rack_id INT NOT NULL,
                         status NVARCHAR(255),
                         created_at DATETIME2(0) NOT NULL,
                         updated_at DATETIME2(0)
);

CREATE TABLE asset_type (
                            type_id INT IDENTITY PRIMARY KEY,
                            type_name NVARCHAR(255) NOT NULL,
                            description NVARCHAR(255),
                            type_class NVARCHAR(255) NOT NULL,
                            status NVARCHAR(40) NOT NULL,
                            default_depreciation_method NVARCHAR(30),
                            default_useful_life_months INT,
                            specification NVARCHAR(255),
                            model NVARCHAR(255),
                            category_id INT NOT NULL
);

/* =========================
   PROCUREMENT FLOW
========================= */

CREATE TABLE purchase_request (
                                  pr_id INT IDENTITY PRIMARY KEY,
                                  status NVARCHAR(40) NOT NULL,
                                  note NVARCHAR(255),
                                  creator_id INT NOT NULL,
                                  needed_by_date DATETIME2(0),
                                  priority NVARCHAR(255) NOT NULL,
                                  approved_by_director_id INT,
                                  reject_reason NVARCHAR(255),
                                  created_at DATETIME2(0) NOT NULL,
                                  updated_at DATETIME2(0),
                                  reason NVARCHAR(255),
                                  approved_at DATETIME2(0),
                                  purchase_staff_id INT
);

CREATE TABLE purchase_request_detail (
                                         pr_detail_id INT IDENTITY PRIMARY KEY,
                                         quantity INT NOT NULL,
                                         pr_id INT NOT NULL,
                                         asset_type_id INT NOT NULL,
                                         spec_requirement NVARCHAR(255),
                                         note NVARCHAR(255)
);

CREATE TABLE quotation (
                           quotation_id INT IDENTITY PRIMARY KEY,
                           pr_id INT NOT NULL,
                           supplier_id INT NOT NULL,
                           status NVARCHAR(255) NOT NULL,
                           note NVARCHAR(255),
                           created_at DATETIME2(0) NOT NULL,
                           updated_at DATETIME2(0),
                           created_by INT NOT NULL
);

CREATE TABLE quotation_detail (
                                  quotation_detail_id INT IDENTITY PRIMARY KEY,
                                  quotation_id INT NOT NULL,
                                  asset_type_id INT NOT NULL,
                                  quantity INT NOT NULL,
                                  note NVARCHAR(255),
                                  warranty_months INT NOT NULL,
                                  price NUMERIC(19,0),
                                  tax_rate NUMERIC(5,2),
                                  discount_rate NUMERIC(19,0)
);

CREATE TABLE purchase_orders (
                                 po_id INT IDENTITY PRIMARY KEY,
                                 note NVARCHAR(255),
                                 status NVARCHAR(40) NOT NULL,
                                 created_at DATETIME2(0) NOT NULL,
                                 pr_id INT,
                                 quotation_id INT,
                                 approved_by INT,
                                 updated_at DATETIME2(0),
                                 updated_by INT
);

CREATE TABLE purchase_order_details (
                                        po_detail_id INT IDENTITY PRIMARY KEY,
                                        quantity INT NOT NULL,
                                        price NUMERIC(19,0) NOT NULL,
                                        tax_rate NUMERIC(5,2),
                                        po_id INT NOT NULL,
                                        asset_type_id INT NOT NULL,
                                        discount_rate NUMERIC(19,0),
                                        note NVARCHAR(255),
                                        warranty_months INT
);

CREATE TABLE goods_receipt (
                               receipt_id INT IDENTITY PRIMARY KEY,
                               receipt_date DATETIME2(0) NOT NULL,
                               po_id INT,
                               received_by INT NOT NULL,
                               warehouse_id INT NOT NULL,
                               status NVARCHAR(40) NOT NULL,
                               created_at DATETIME2(0) NOT NULL,
                               updated_at DATETIME2(0)
);

CREATE TABLE goods_receipt_details (
                                       receipt_detail_id INT IDENTITY PRIMARY KEY,
                                       receipt_id INT NOT NULL,
                                       po_detail_id INT NOT NULL,
                                       asset_type_id INT NOT NULL,
                                       received_quantity INT NOT NULL,
                                       accepted_quantity INT NOT NULL,
                                       rejected_quantity INT NOT NULL,
                                       condition_on_arrival NVARCHAR(40) NOT NULL,
                                       price NUMERIC(19,0) NOT NULL,
                                       note NVARCHAR(255)
);


CREATE TABLE asset (
                       asset_id INT IDENTITY PRIMARY KEY,
                       asset_name NVARCHAR(100) NOT NULL,
                       serial_number NVARCHAR(100) UNIQUE,
                       current_status NVARCHAR(40) NOT NULL,
                       warranty_end_date DATE,
                       original_cost NUMERIC(19,0),
                       shelf_id INT NULL,
                       asset_type_id INT NOT NULL,
                       receipt_request_id INT NULL,
                       acquisition_date DATE,
                       in_service_date DATE,
                       warranty_start_date DATE,
                       warehouse_id INT NULL,
                       department_id INT NULL
);

CREATE TABLE allocation_request (
                                    request_id INT IDENTITY PRIMARY KEY,
                                    request_date DATETIME2(0) NOT NULL,
                                    status NVARCHAR(255) NOT NULL,
                                    reason NVARCHAR(255) NOT NULL,
                                    reason_reject NVARCHAR(255),
                                    department_id INT NOT NULL,
                                    needed_by_date DATETIME2(0),
                                    am_approved_by INT,
                                    am_approved_at DATETIME2(0),
                                    created_at DATETIME2(0) NOT NULL,
                                    updated_at DATETIME2(0),
                                    quantity INT,
                                    priority NVARCHAR(255),
                                    creator_id INT,

                                    CONSTRAINT FK_allocation_request_department
                                        FOREIGN KEY (department_id) REFERENCES departments(department_id),

                                    CONSTRAINT FK_allocation_request_am_approved
                                        FOREIGN KEY (am_approved_by) REFERENCES users(user_id),

                                    CONSTRAINT FK_allocation_request_creator
                                        FOREIGN KEY (creator_id) REFERENCES users(user_id)
);

CREATE TABLE allocation_request_details (
                                            allocation_detail_id INT IDENTITY PRIMARY KEY,
                                            allocation_request_id INT NOT NULL,
                                            note NVARCHAR(255),
                                            asset_id INT
);

CREATE TABLE stock_issue (
                             stock_issue_id INT IDENTITY PRIMARY KEY,
                             warehouse_id INT NOT NULL,
                             allocation_request_id INT NOT NULL,
                             status NVARCHAR(40) NOT NULL,
                             issued_by INT NOT NULL,
                             created_at DATETIME2(0) NOT NULL,
                             updated_at DATETIME2(0),
                             issued_to_department INT
);

CREATE TABLE stock_issue_detail (
                                    stock_issue_detail_id INT IDENTITY PRIMARY KEY,
                                    stock_issue_id INT NOT NULL,
                                    asset_id INT NOT NULL,
                                    quantity INT NOT NULL,
                                    note NVARCHAR(255),
                                    unit_value NUMERIC(19,0),
                                    issued_condition NVARCHAR(40)
);

CREATE TABLE revocation (
                            revocation_id INT IDENTITY PRIMARY KEY,
                            status NVARCHAR(40) NOT NULL,
                            reason NVARCHAR(255),
                            note NVARCHAR(255),
                            created_at DATETIME2(0) NOT NULL,
                            updated_at DATETIME2(0),
                            revoked_from_department_id INT NOT NULL,
                            created_by INT NOT NULL
);

CREATE TABLE revocation_detail (
                                   revocation_detail_id INT IDENTITY PRIMARY KEY,
                                   returned_condition NVARCHAR(255) NOT NULL,
                                   note NVARCHAR(255),
                                   revocation_id INT NOT NULL,
                                   asset_id INT NOT NULL,
                                   damage_level NVARCHAR(255)
);

CREATE TABLE asset_logs (
                            log_id INT IDENTITY PRIMARY KEY,
                            action_type NVARCHAR(155) NOT NULL,
                            from_department INT,
                            note NVARCHAR(255),
                            action_date DATETIME2(0) NOT NULL,
                            asset_id INT NOT NULL,
                            old_status NVARCHAR(40),
                            created_at DATETIME2(0),
                            to_department INT,
                            related_allocation_id INT NOT NULL
);

CREATE TABLE asset_department (
                                  asset_users_id INT IDENTITY PRIMARY KEY,
                                  status NVARCHAR(40) NOT NULL,
                                  note NVARCHAR(255),
                                  asset_id INT NOT NULL,
                                  assigned_to_dept_id INT NOT NULL,
                                  assigned_from_dept_id INT NOT NULL,
                                  updated_at DATETIME2(0),
                                  related_allocation_id INT NOT NULL
);



ALTER TABLE departments ADD FOREIGN KEY (manager_id) REFERENCES users(user_id);

ALTER TABLE warehouse ADD FOREIGN KEY (user_id) REFERENCES users(user_id);
ALTER TABLE rack ADD FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id);
ALTER TABLE shelves ADD FOREIGN KEY (rack_id) REFERENCES rack(rack_id);

ALTER TABLE asset_type ADD FOREIGN KEY (category_id) REFERENCES category(category_id);

ALTER TABLE purchase_request ADD FOREIGN KEY (creator_id) REFERENCES users(user_id);
ALTER TABLE purchase_request ADD FOREIGN KEY (approved_by_director_id) REFERENCES users(user_id);
ALTER TABLE purchase_request ADD FOREIGN KEY (purchase_staff_id) REFERENCES users(user_id);

ALTER TABLE purchase_request_detail ADD FOREIGN KEY (pr_id) REFERENCES purchase_request(pr_id);
ALTER TABLE purchase_request_detail ADD FOREIGN KEY (asset_type_id) REFERENCES asset_type(type_id);

ALTER TABLE quotation ADD FOREIGN KEY (pr_id) REFERENCES purchase_request(pr_id);
ALTER TABLE quotation ADD FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id);
ALTER TABLE quotation ADD FOREIGN KEY (created_by) REFERENCES users(user_id);

ALTER TABLE quotation_detail ADD FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id);
ALTER TABLE quotation_detail ADD FOREIGN KEY (asset_type_id) REFERENCES asset_type(type_id);

ALTER TABLE purchase_orders ADD FOREIGN KEY (pr_id) REFERENCES purchase_request(pr_id);
ALTER TABLE purchase_orders ADD FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id);
ALTER TABLE purchase_orders ADD FOREIGN KEY (approved_by) REFERENCES users(user_id);

ALTER TABLE purchase_order_details ADD FOREIGN KEY (po_id) REFERENCES purchase_orders(po_id);
ALTER TABLE purchase_order_details ADD FOREIGN KEY (asset_type_id) REFERENCES asset_type(type_id);

ALTER TABLE goods_receipt ADD FOREIGN KEY (po_id) REFERENCES purchase_orders(po_id);
ALTER TABLE goods_receipt ADD FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id);
ALTER TABLE goods_receipt ADD FOREIGN KEY (received_by) REFERENCES users(user_id);

ALTER TABLE goods_receipt_details ADD FOREIGN KEY (receipt_id) REFERENCES goods_receipt(receipt_id);
ALTER TABLE goods_receipt_details ADD FOREIGN KEY (po_detail_id) REFERENCES purchase_order_details(po_detail_id);
ALTER TABLE goods_receipt_details ADD FOREIGN KEY (asset_type_id) REFERENCES asset_type(type_id);

ALTER TABLE asset ADD FOREIGN KEY (shelf_id) REFERENCES shelves(shelf_id);
ALTER TABLE asset ADD FOREIGN KEY (asset_type_id) REFERENCES asset_type(type_id);
ALTER TABLE asset ADD FOREIGN KEY (receipt_request_id) REFERENCES goods_receipt(receipt_id);
ALTER TABLE asset ADD FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id);
ALTER TABLE asset ADD FOREIGN KEY (department_id) REFERENCES departments(department_id);

ALTER TABLE allocation_request ADD FOREIGN KEY (department_id) REFERENCES departments(department_id);
ALTER TABLE allocation_request ADD FOREIGN KEY (am_approved_by) REFERENCES users(user_id);

ALTER TABLE allocation_request_details ADD FOREIGN KEY (allocation_request_id) REFERENCES allocation_request(request_id);
ALTER TABLE allocation_request_details ADD FOREIGN KEY (asset_id) REFERENCES asset(asset_id);

ALTER TABLE stock_issue ADD FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id);
ALTER TABLE stock_issue ADD FOREIGN KEY (allocation_request_id) REFERENCES allocation_request(request_id);
ALTER TABLE stock_issue ADD FOREIGN KEY (issued_by) REFERENCES users(user_id);

ALTER TABLE stock_issue_detail ADD FOREIGN KEY (stock_issue_id) REFERENCES stock_issue(stock_issue_id);
ALTER TABLE stock_issue_detail ADD FOREIGN KEY (asset_id) REFERENCES asset(asset_id);

ALTER TABLE revocation ADD FOREIGN KEY (revoked_from_department_id) REFERENCES departments(department_id);
ALTER TABLE revocation ADD FOREIGN KEY (created_by) REFERENCES users(user_id);

ALTER TABLE revocation_detail ADD FOREIGN KEY (revocation_id) REFERENCES revocation(revocation_id);
ALTER TABLE revocation_detail ADD FOREIGN KEY (asset_id) REFERENCES asset(asset_id);

ALTER TABLE asset_logs ADD FOREIGN KEY (asset_id) REFERENCES asset(asset_id);
ALTER TABLE asset_logs ADD FOREIGN KEY (from_department) REFERENCES departments(department_id);
ALTER TABLE asset_logs ADD FOREIGN KEY (to_department) REFERENCES departments(department_id);

ALTER TABLE asset_department ADD FOREIGN KEY (asset_id) REFERENCES asset(asset_id);
ALTER TABLE asset_department ADD FOREIGN KEY (assigned_to_dept_id) REFERENCES departments(department_id);
ALTER TABLE asset_department ADD FOREIGN KEY (assigned_from_dept_id) REFERENCES departments(department_id);
