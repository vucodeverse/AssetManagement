-- 1. Thêm Department mẫu (bắt buộc cho User)
INSERT INTO departments (department_name, status, description)
VALUES (N'Phòng Quản Lý Kho', N'ACTIVE', N'Phòng ban phụ trách các kho bãi trong công ty');

-- Lấy ID department vừa thêm (Giả sử id là 1)
DECLARE @DeptId INT = (SELECT TOP 1 department_id FROM departments ORDER BY department_id DESC);

-- 2. Thêm Users mẫu (Warehouse Staff & Asset Manager)
INSERT INTO users (username, password_hash, first_name, last_name, phone_number, email, status, role, department_id)
VALUES 
('nvkho1', 'hashed_pwd_123', N'Nguyễn Văn', N'A', '0901234567', 'nva@company.com', N'ACTIVE', 'WAREHOUSE_STAFF', @DeptId),
('nvkho2', 'hashed_pwd_456', N'Trần Thị', N'B', '0912345678', 'ttb@company.com', N'ACTIVE', 'WAREHOUSE_STAFF', @DeptId),
('qlts1', 'hashed_pwd_789', N'Lê Hoàng', N'C', '0923456789', 'lhc@company.com', N'ACTIVE', 'ASSET_MANAGER', @DeptId);

-- 3. Thêm Category mẫu (bắt buộc cho Asset Type)
INSERT INTO category (category_name, description, status)
VALUES 
(N'Thiết bị điện tử', N'Các thiết bị điện tử, máy tính', N'ACTIVE'),
(N'Nội thất', N'Bàn, ghế, tủ', N'ACTIVE');

DECLARE @CatId1 INT = (SELECT TOP 1 category_id FROM category WHERE category_name = N'Thiết bị điện tử');
DECLARE @CatId2 INT = (SELECT TOP 1 category_id FROM category WHERE category_name = N'Nội thất');

-- 4. Thêm Asset Type mẫu
INSERT INTO asset_type (type_name, description, type_class, status, category_id)
VALUES 
(N'Laptop Dell Latitude', N'Laptop cho nhân viên', N'ELECTRONICS', N'ACTIVE', @CatId1),
(N'Màn hình Dell 24 inch', N'Màn hình máy tính', N'ELECTRONICS', N'ACTIVE', @CatId1),
(N'Bàn làm việc 1m2', N'Bàn văn phòng', N'FURNITURE', N'ACTIVE', @CatId2);

-- Sau khi chạy các lệnh trên, hãy F5 lại giao diện Form Thêm Kho và Thêm Zone, các dropdown sẽ có dữ liệu!
