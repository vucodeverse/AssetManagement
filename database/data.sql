-- =============================================
-- KHỞI TẠO DỮ LIỆU (SEED DATA)
-- =============================================

INSERT INTO category (category_name, description, status)
VALUES 
(N'Laptop', N'Thiết bị laptop', N'ACTIVE'),
(N'Monitor', N'Màn hình', N'ACTIVE');

INSERT INTO asset_type (
    type_name,
    description,
    type_class,
    status,
    category_id,
    model
)
VALUES
(N'Dell Laptop', N'Laptop Dell', N'IT', N'ACTIVE', 1, N'Dell Latitude'),
(N'LG Monitor', N'Màn hình LG', N'IT', N'ACTIVE', 2, N'LG 24inch');

INSERT INTO departments (department_name, status)
VALUES 
(N'IT', N'ACTIVE'),
(N'HR', N'ACTIVE');

INSERT INTO users (
    username,
    password_hash,
    first_name,
    last_name,
    email,
    status,
    role,
    department_id
)
VALUES
(N'admin', N'123', N'Admin', N'User', 'admin@mail.com', N'ACTIVE', N'ADMIN', 1),
(N'user1', N'123', N'User', N'One', 'user1@mail.com', N'ACTIVE', N'STAFF', 1);

-- Cập nhật manager_user_id sau khi đã có users
UPDATE departments SET manager_user_id = 1 WHERE department_id = 1;
UPDATE departments SET manager_user_id = 2 WHERE department_id = 2; -- Giả định user1 làm manager cho HR nếu cần, hoặc cứ để NULL

INSERT INTO supplier (
    supplier_name,
    phone_number,
    email,
    address,
    tax_code,
    status
)
VALUES
(N'Dell VN', '0123', 'dell@mail.com', N'HN', 'TAX1', N'ACTIVE');

INSERT INTO purchase_request (
    status,
    creator_id,
    priority
)
VALUES
(N'APPROVED', 1, N'HIGH');

INSERT INTO purchase_request_detail (
    estimated_price,
    quantity,
    purchase_request_id,
    asset_type_id
)
VALUES
(1000, 5, 1, 1),
(200, 5, 1, 2);

INSERT INTO quotation (
    purchase_request_id,
    supplier_id,
    status
)
VALUES
(1, 1, N'APPROVED');

INSERT INTO quotation_detail (
    quotation_id,
    purchase_request_detail_id,
    asset_type_id,
    quantity,
    price
)
VALUES
(1, 1, 1, 5, 1000),
(1, 2, 2, 5, 200);

INSERT INTO purchase_orders (
    status,
    purchase_request_id,
    supplier_id,
    quotation_id
)
VALUES
(N'APPROVED', 1, 1, 1);

INSERT INTO purchase_order_details (
    quantity,
    unit_price,
    purchase_order_id,
    asset_type_id
)
VALUES
(5, 1000, 1, 1),
(5, 200, 1, 2);

INSERT INTO asset (
    asset_name,
    asset_type_id,
    purchase_order_detail_id,
    current_status,
    department_id
)
VALUES
(N'Laptop 1', 1, 1, N'IN_STOCK', 1),
(N'Laptop 2', 1, 1, N'IN_STOCK', 1),
(N'Laptop 3', 1, 1, N'IN_STOCK', 1),
(N'Laptop 4', 1, 1, N'IN_STOCK', 1),
(N'Laptop 5', 1, 1, N'IN_STOCK', 1),

(N'Monitor 1', 2, 2, N'IN_STOCK', 2),
(N'Monitor 2', 2, 2, N'IN_STOCK', 2),
(N'Monitor 3', 2, 2, N'IN_STOCK', 2),
(N'Monitor 4', 2, 2, N'IN_STOCK', 2),
(N'Monitor 5', 2, 2, N'IN_STOCK', 2);
