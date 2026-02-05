CREATE TABLE users (
  user_id INT IDENTITY NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(150) NOT NULL,
  phone_number VARCHAR(30) NULL,
  email VARCHAR(100) NULL UNIQUE,
  created_date DATE NOT NULL,
  status VARCHAR(40) NOT NULL,
  role VARCHAR(40) NOT NULL,
  updated_date DATE NULL,
  PRIMARY KEY (user_id)
);

CREATE TABLE departments (
  department_id INT IDENTITY NOT NULL,
  department_name VARCHAR(150) NOT NULL,
  manager_user_id INT NOT NULL UNIQUE,
  PRIMARY KEY (department_id)
);

CREATE TABLE category (
  category_id INT IDENTITY NOT NULL,
  category_name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  status VARCHAR(40) NOT NULL,
  PRIMARY KEY (category_id)
);

CREATE TABLE supplier (
  supplier_id INT IDENTITY NOT NULL,
  supplier_name VARCHAR(255) NOT NULL,
  phone_number NVARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  address VARCHAR(255) NOT NULL,
  supplier_code VARCHAR(255) NULL,
  tax_code VARCHAR(255) NULL UNIQUE,
  status VARCHAR(255) NOT NULL,
  created_date DATE NOT NULL,
  updated_date DATE NULL,
  PRIMARY KEY (supplier_id)
);

CREATE TABLE warehouse (
  warehouse_id INT IDENTITY NOT NULL,
  warehouse_name VARCHAR(255) NOT NULL,
  address VARCHAR(255) NULL,
  status VARCHAR(40) NOT NULL,
  managed_by_user_id INT NOT NULL,
  PRIMARY KEY (warehouse_id)
);

CREATE TABLE rack (
  rack_id INT IDENTITY NOT NULL,
  warehouse_id INT NOT NULL,
  rack_name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  status VARCHAR(40) NOT NULL,
  created_date DATE NOT NULL,
  updated_date DATE NULL,
  PRIMARY KEY (rack_id)
);

CREATE TABLE shelf (
  shelf_id INT IDENTITY NOT NULL,
  shelf_name VARCHAR(255) NOT NULL,
  current_capacity INT NOT NULL,
  max_capacity INT NOT NULL,
  description VARCHAR(255) NULL,
  rack_id INT NOT NULL,
  status VARCHAR(255) NULL,
  created_date DATE NOT NULL,
  updated_date DATE NULL,
  PRIMARY KEY (shelf_id)
);

CREATE TABLE asset_type (
  asset_type_id INT IDENTITY NOT NULL,
  type_name NVARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  type_class VARCHAR(255) NOT NULL,
  status VARCHAR(40) NOT NULL,
  default_depreciation_method VARCHAR(30) NULL,
  default_useful_life_months INT NULL,
  specification VARCHAR(255) NULL,
  category_id INT NOT NULL,
  model VARCHAR(255) NULL,
  PRIMARY KEY (asset_type_id)
);

CREATE TABLE purchase_request (
  purchase_request_id INT IDENTITY NOT NULL,
  status VARCHAR(40) NOT NULL,
  note VARCHAR(255) NULL,
  created_by_user_id INT NOT NULL,
  requesting_department_id INT NOT NULL,
  needed_by_date DATE NULL,
  priority VARCHAR(255) NOT NULL,
  approved_by_director_user_id INT NOT NULL,
  reject_reason VARCHAR(255) NULL,
  created_date DATE NOT NULL,
  updated_date DATE NULL,
  PRIMARY KEY (purchase_request_id)
);

CREATE TABLE purchase_request_detail (
  purchase_request_detail_id INT IDENTITY NOT NULL,
  purchase_request_id INT NOT NULL,
  asset_type_id INT NOT NULL,
  quantity INT NOT NULL,
  specification_requirement VARCHAR(255) NULL,
  note VARCHAR(255) NULL,
  PRIMARY KEY (purchase_request_detail_id)
);

CREATE TABLE quotation (
  quotation_id INT IDENTITY NOT NULL,
  purchase_request_id INT NOT NULL,
  supplier_id INT NOT NULL,
  quotation_date DATE NOT NULL,
  status VARCHAR(255) NOT NULL,
  total_amount NUMERIC(19, 0) NULL,
  note VARCHAR(255) NULL,
  created_date DATE NOT NULL,
  updated_date DATE NULL,
  PRIMARY KEY (quotation_id)
);

CREATE TABLE quotation_detail (
  quotation_detail_id INT IDENTITY NOT NULL,
  quotation_id INT NOT NULL,
  asset_type_id INT NOT NULL,
  quantity INT NOT NULL,
  note VARCHAR(255) NULL,
  warranty_months INT NOT NULL,
  price NUMERIC(19, 0) NULL,
  PRIMARY KEY (quotation_detail_id)
);

CREATE TABLE purchase_order (
  purchase_order_id INT IDENTITY NOT NULL,
  order_date DATE NOT NULL,
  total_amount NUMERIC(19, 0) NULL,
  note VARCHAR(255) NULL,
  status VARCHAR(40) NOT NULL,
  created_date DATE NOT NULL,
  purchase_request_id INT NOT NULL,
  supplier_id INT NOT NULL,
  quotation_id INT NOT NULL,
  approved_by_user_id INT NOT NULL,
  updated_date DATE NULL,
  updated_by_user_id INT NULL,
  PRIMARY KEY (purchase_order_id)
);

CREATE TABLE purchase_order_detail (
  purchase_order_detail_id INT IDENTITY NOT NULL,
  purchase_order_id INT NOT NULL,
  asset_type_id INT NOT NULL,
  quantity INT NOT NULL,
  unit_price NUMERIC(19, 0) NOT NULL,
  tax_rate NUMERIC(5, 2) NULL,
  discount NUMERIC(19, 0) NULL,
  note VARCHAR(255) NULL,
  PRIMARY KEY (purchase_order_detail_id)
);

CREATE TABLE goods_receipt (
  goods_receipt_id INT IDENTITY NOT NULL,
  receipt_date DATE NOT NULL,
  purchase_order_id INT NOT NULL,
  received_by_user_id INT NOT NULL,
  warehouse_id INT NOT NULL,
  inspected_by_user_id INT NOT NULL,
  status VARCHAR(40) NOT NULL,
  created_date DATE NOT NULL,
  updated_date DATE NULL,
  PRIMARY KEY (goods_receipt_id)
);

CREATE TABLE goods_receipt_detail (
  goods_receipt_detail_id INT IDENTITY NOT NULL,
  goods_receipt_id INT NOT NULL,
  purchase_order_detail_id INT NOT NULL,
  asset_type_id INT NOT NULL,
  received_quantity INT NOT NULL,
  accepted_quantity INT NOT NULL,
  rejected_quantity INT NOT NULL,
  condition_on_arrival VARCHAR(40) NOT NULL,
  unit_price NUMERIC(19, 0) NOT NULL,
  note VARCHAR(255) NULL,
  PRIMARY KEY (goods_receipt_detail_id)
);

CREATE TABLE asset (
  asset_id INT IDENTITY NOT NULL,
  serial_number VARCHAR(100) NULL UNIQUE,
  current_status VARCHAR(40) NOT NULL,
  warranty_start_date DATE NULL,
  warranty_end_date DATE NULL,
  original_cost NUMERIC(19, 0) NULL,
  shelf_id INT NOT NULL,
  asset_type_id INT NOT NULL,
  goods_receipt_id INT NOT NULL,
  acquisition_date DATE NULL,
  in_service_date DATE NULL,
  warehouse_id INT NULL,
  department_id INT NULL,
  PRIMARY KEY (asset_id)
);

CREATE TABLE allocation_request (
  allocation_request_id INT IDENTITY NOT NULL,
  request_date DATE NOT NULL,
  status VARCHAR(255) NOT NULL,
  request_reason VARCHAR(255) NOT NULL,
  reject_reason VARCHAR(255) NULL,
  approved_by_user_id INT NOT NULL,
  requested_department_id INT NOT NULL,
  needed_by_date DATE NULL,
  department_manager_approved_by_user_id INT NOT NULL,
  department_manager_approved_date DATE NULL,
  asset_manager_approved_by_user_id INT NOT NULL,
  asset_manager_approved_date DATE NULL,
  created_date DATE NULL,
  updated_date DATE NOT NULL,
  PRIMARY KEY (allocation_request_id)
);

CREATE TABLE allocation_request_details (
  allocation_request_detail_id INT IDENTITY NOT NULL,
  allocation_request_id INT NOT NULL,
  asset_type_id INT NOT NULL,
  requested_quantity INT NOT NULL,
  issued_condition VARCHAR(40) NOT NULL,
  note NVARCHAR(255) NULL,
  PRIMARY KEY (allocation_request_detail_id)
);

CREATE TABLE allocation (
  allocation_id INT IDENTITY NOT NULL,
  allocated_by_user_id INT NOT NULL,
  allocated_to_department_id INT NOT NULL,
  allocated_from_department_id INT NOT NULL,
  allocation_date DATE NOT NULL,
  status VARCHAR(40) NOT NULL,
  note VARCHAR(255) NULL,
  created_date DATE NOT NULL,
  allocation_request_id INT NOT NULL,
  sign_off_date DATE NULL,
  created_by_user_id INT NOT NULL,
  updated_date DATE NULL,
  PRIMARY KEY (allocation_id)
);

CREATE TABLE allocation_detail (
  allocation_detail_id INT IDENTITY NOT NULL,
  allocation_id INT NOT NULL,
  asset_id INT NOT NULL,
  issued_condition VARCHAR(255) NULL,
  note VARCHAR(255) NULL,
  PRIMARY KEY (allocation_detail_id)
);

CREATE TABLE asset_department_assignment (
  asset_department_assignment_id INT IDENTITY NOT NULL,
  status VARCHAR(40) NOT NULL,
  note VARCHAR(255) NULL,
  asset_id INT NOT NULL,
  assigned_to_department_id INT NOT NULL,
  assigned_from_department_id INT NOT NULL,
  updated_date DATE NULL,
  related_allocation_id INT NOT NULL,
  PRIMARY KEY (asset_department_assignment_id)
);

CREATE TABLE asset_log (
  asset_log_id INT IDENTITY NOT NULL,
  action_type VARCHAR(155) NOT NULL,
  from_department_id INT NULL,
  to_department_id INT NULL,
  note VARCHAR(255) NULL,
  action_date DATE NOT NULL,
  asset_id INT NOT NULL,
  old_status VARCHAR(40) NULL,
  created_date DATE NULL,
  related_allocation_id INT NOT NULL,
  PRIMARY KEY (asset_log_id)
);

CREATE TABLE stock_issue (
  stock_issue_id INT IDENTITY NOT NULL,
  warehouse_id INT NOT NULL,
  related_allocation_id INT NOT NULL,
  status VARCHAR(40) NOT NULL,
  issued_by_user_id INT NOT NULL,
  created_date DATE NULL,
  updated_date DATE NULL,
  PRIMARY KEY (stock_issue_id)
);

CREATE TABLE stock_issue_detail (
  stock_issue_detail_id INT IDENTITY NOT NULL,
  stock_issue_id INT NOT NULL,
  asset_id INT NOT NULL,
  quantity INT NOT NULL,
  note VARCHAR(255) NULL,
  PRIMARY KEY (stock_issue_detail_id)
);

CREATE TABLE revocation (
  revocation_id INT IDENTITY NOT NULL,
  revocation_date DATE NOT NULL,
  status VARCHAR(40) NOT NULL,
  reason VARCHAR(255) NULL,
  note VARCHAR(255) NULL,
  created_date DATE NOT NULL,
  updated_date DATE NULL,
  revoked_from_department_id INT NOT NULL,
  created_by_user_id INT NOT NULL,
  PRIMARY KEY (revocation_id)
);

CREATE TABLE revocation_detail (
  revocation_detail_id INT IDENTITY NOT NULL,
  revocation_id INT NOT NULL,
  asset_id INT NOT NULL,
  returned_quantity INT NOT NULL,
  returned_condition VARCHAR(255) NOT NULL,
  damage_level VARCHAR(255) NULL,
  note VARCHAR(255) NULL,
  PRIMARY KEY (revocation_detail_id)
);

ALTER TABLE departments
ADD CONSTRAINT FK_departments_manager_user
FOREIGN KEY (manager_user_id) REFERENCES users(user_id);

ALTER TABLE warehouse
ADD CONSTRAINT FK_warehouse_managed_by_user
FOREIGN KEY (managed_by_user_id) REFERENCES users(user_id);

ALTER TABLE rack
ADD CONSTRAINT FK_rack_warehouse
FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id);

ALTER TABLE shelf
ADD CONSTRAINT FK_shelf_rack
FOREIGN KEY (rack_id) REFERENCES rack(rack_id);

ALTER TABLE asset_type
ADD CONSTRAINT FK_asset_type_category
FOREIGN KEY (category_id) REFERENCES category(category_id);

ALTER TABLE purchase_request
ADD CONSTRAINT FK_purchase_request_created_by_user
FOREIGN KEY (created_by_user_id) REFERENCES users(user_id);

ALTER TABLE purchase_request
ADD CONSTRAINT FK_purchase_request_department
FOREIGN KEY (requesting_department_id) REFERENCES departments(department_id);

ALTER TABLE purchase_request
ADD CONSTRAINT FK_purchase_request_approved_by_director_user
FOREIGN KEY (approved_by_director_user_id) REFERENCES users(user_id);

ALTER TABLE purchase_request_detail
ADD CONSTRAINT FK_purchase_request_detail_request
FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id);

ALTER TABLE purchase_request_detail
ADD CONSTRAINT FK_purchase_request_detail_asset_type
FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id);

ALTER TABLE quotation
ADD CONSTRAINT FK_quotation_purchase_request
FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id);

ALTER TABLE quotation
ADD CONSTRAINT FK_quotation_supplier
FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id);

ALTER TABLE quotation_detail
ADD CONSTRAINT FK_quotation_detail_quotation
FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id);

ALTER TABLE quotation_detail
ADD CONSTRAINT FK_quotation_detail_asset_type
FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id);

ALTER TABLE purchase_order
ADD CONSTRAINT FK_purchase_order_purchase_request
FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(purchase_request_id);

ALTER TABLE purchase_order
ADD CONSTRAINT FK_purchase_order_supplier
FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id);

ALTER TABLE purchase_order
ADD CONSTRAINT FK_purchase_order_quotation
FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id);

ALTER TABLE purchase_order
ADD CONSTRAINT FK_purchase_order_approved_by_user
FOREIGN KEY (approved_by_user_id) REFERENCES users(user_id);

ALTER TABLE purchase_order
ADD CONSTRAINT FK_purchase_order_updated_by_user
FOREIGN KEY (updated_by_user_id) REFERENCES users(user_id);

ALTER TABLE purchase_order_detail
ADD CONSTRAINT FK_purchase_order_detail_order
FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(purchase_order_id);

ALTER TABLE purchase_order_detail
ADD CONSTRAINT FK_purchase_order_detail_asset_type
FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id);

ALTER TABLE goods_receipt
ADD CONSTRAINT FK_goods_receipt_purchase_order
FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(purchase_order_id);

ALTER TABLE goods_receipt
ADD CONSTRAINT FK_goods_receipt_received_by_user
FOREIGN KEY (received_by_user_id) REFERENCES users(user_id);

ALTER TABLE goods_receipt
ADD CONSTRAINT FK_goods_receipt_inspected_by_user
FOREIGN KEY (inspected_by_user_id) REFERENCES users(user_id);

ALTER TABLE goods_receipt
ADD CONSTRAINT FK_goods_receipt_warehouse
FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id);

ALTER TABLE goods_receipt_detail
ADD CONSTRAINT FK_goods_receipt_detail_receipt
FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipt(goods_receipt_id);

ALTER TABLE goods_receipt_detail
ADD CONSTRAINT FK_goods_receipt_detail_purchase_order_detail
FOREIGN KEY (purchase_order_detail_id) REFERENCES purchase_order_detail(purchase_order_detail_id);

ALTER TABLE goods_receipt_detail
ADD CONSTRAINT FK_goods_receipt_detail_asset_type
FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id);

ALTER TABLE asset
ADD CONSTRAINT FK_asset_shelf
FOREIGN KEY (shelf_id) REFERENCES shelf(shelf_id);

ALTER TABLE asset
ADD CONSTRAINT FK_asset_asset_type
FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id);

ALTER TABLE asset
ADD CONSTRAINT FK_asset_goods_receipt
FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipt(goods_receipt_id);

ALTER TABLE asset
ADD CONSTRAINT FK_asset_warehouse
FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id);

ALTER TABLE asset
ADD CONSTRAINT FK_asset_department
FOREIGN KEY (department_id) REFERENCES departments(department_id);

ALTER TABLE allocation_request
ADD CONSTRAINT FK_allocation_request_requested_department
FOREIGN KEY (requested_department_id) REFERENCES departments(department_id);

ALTER TABLE allocation_request
ADD CONSTRAINT FK_allocation_request_approved_by_user
FOREIGN KEY (approved_by_user_id) REFERENCES users(user_id);

ALTER TABLE allocation_request
ADD CONSTRAINT FK_allocation_request_department_manager_user
FOREIGN KEY (department_manager_approved_by_user_id) REFERENCES users(user_id);

ALTER TABLE allocation_request
ADD CONSTRAINT FK_allocation_request_asset_manager_user
FOREIGN KEY (asset_manager_approved_by_user_id) REFERENCES users(user_id);

ALTER TABLE allocation_request_details
ADD CONSTRAINT FK_allocation_request_details_request
FOREIGN KEY (allocation_request_id) REFERENCES allocation_request(allocation_request_id);

ALTER TABLE allocation_request_details
ADD CONSTRAINT FK_allocation_request_details_asset_type
FOREIGN KEY (asset_type_id) REFERENCES asset_type(asset_type_id);

ALTER TABLE allocation
ADD CONSTRAINT FK_allocation_allocated_by_user
FOREIGN KEY (allocated_by_user_id) REFERENCES users(user_id);

ALTER TABLE allocation
ADD CONSTRAINT FK_allocation_created_by_user
FOREIGN KEY (created_by_user_id) REFERENCES users(user_id);

ALTER TABLE allocation
ADD CONSTRAINT FK_allocation_allocated_to_department
FOREIGN KEY (allocated_to_department_id) REFERENCES departments(department_id);

ALTER TABLE allocation
ADD CONSTRAINT FK_allocation_allocated_from_department
FOREIGN KEY (allocated_from_department_id) REFERENCES departments(department_id);

ALTER TABLE allocation
ADD CONSTRAINT FK_allocation_allocation_request
FOREIGN KEY (allocation_request_id) REFERENCES allocation_request(allocation_request_id);

ALTER TABLE allocation_detail
ADD CONSTRAINT FK_allocation_detail_allocation
FOREIGN KEY (allocation_id) REFERENCES allocation(allocation_id);

ALTER TABLE allocation_detail
ADD CONSTRAINT FK_allocation_detail_asset
FOREIGN KEY (asset_id) REFERENCES asset(asset_id);

ALTER TABLE asset_department_assignment
ADD CONSTRAINT FK_asset_department_assignment_asset
FOREIGN KEY (asset_id) REFERENCES asset(asset_id);

ALTER TABLE asset_department_assignment
ADD CONSTRAINT FK_asset_department_assignment_to_department
FOREIGN KEY (assigned_to_department_id) REFERENCES departments(department_id);

ALTER TABLE asset_department_assignment
ADD CONSTRAINT FK_asset_department_assignment_from_department
FOREIGN KEY (assigned_from_department_id) REFERENCES departments(department_id);

ALTER TABLE asset_department_assignment
ADD CONSTRAINT FK_asset_department_assignment_related_allocation
FOREIGN KEY (related_allocation_id) REFERENCES allocation(allocation_id);

ALTER TABLE asset_log
ADD CONSTRAINT FK_asset_log_asset
FOREIGN KEY (asset_id) REFERENCES asset(asset_id);

ALTER TABLE asset_log
ADD CONSTRAINT FK_asset_log_from_department
FOREIGN KEY (from_department_id) REFERENCES departments(department_id);

ALTER TABLE asset_log
ADD CONSTRAINT FK_asset_log_to_department
FOREIGN KEY (to_department_id) REFERENCES departments(department_id);

ALTER TABLE asset_log
ADD CONSTRAINT FK_asset_log_related_allocation
FOREIGN KEY (related_allocation_id) REFERENCES allocation(allocation_id);

ALTER TABLE stock_issue
ADD CONSTRAINT FK_stock_issue_warehouse
FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id);

ALTER TABLE stock_issue
ADD CONSTRAINT FK_stock_issue_related_allocation
FOREIGN KEY (related_allocation_id) REFERENCES allocation(allocation_id);

ALTER TABLE stock_issue
ADD CONSTRAINT FK_stock_issue_issued_by_user
FOREIGN KEY (issued_by_user_id) REFERENCES users(user_id);

ALTER TABLE stock_issue_detail
ADD CONSTRAINT FK_stock_issue_detail_stock_issue
FOREIGN KEY (stock_issue_id) REFERENCES stock_issue(stock_issue_id);

ALTER TABLE stock_issue_detail
ADD CONSTRAINT FK_stock_issue_detail_asset
FOREIGN KEY (asset_id) REFERENCES asset(asset_id);

ALTER TABLE revocation
ADD CONSTRAINT FK_revocation_revoked_from_department
FOREIGN KEY (revoked_from_department_id) REFERENCES departments(department_id);

ALTER TABLE revocation
ADD CONSTRAINT FK_revocation_created_by_user
FOREIGN KEY (created_by_user_id) REFERENCES users(user_id);

ALTER TABLE revocation_detail
ADD CONSTRAINT FK_revocation_detail_revocation
FOREIGN KEY (revocation_id) REFERENCES revocation(revocation_id);

ALTER TABLE revocation_detail
ADD CONSTRAINT FK_revocation_detail_asset
FOREIGN KEY (asset_id) REFERENCES asset(asset_id);
