-- =============================================
-- DUMMY DATA FOR PO INBOUND TESTING
-- =============================================

-- 1. Thêm Nhà cung cấp (Supplier)
IF NOT EXISTS (SELECT 1 FROM supplier WHERE supplier_name = N'Phong Vũ IT')
BEGIN
    INSERT INTO supplier (supplier_name, phone_number, email, address, supplier_code, tax_code, status)
    VALUES (N'Phong Vũ IT', '0123456789', 'contact@phongvu.vn', N'264 Nguyễn Thị Minh Khai, Q3, HCM', 'SUP001', 'TAX001', 'ACTIVE');
END

-- 2. Thêm Loại tài sản (Asset Type)
DECLARE @CatID INT;
SELECT TOP 1 @CatID = category_id FROM category WHERE category_name = N'Thiết bị IT';
IF @CatID IS NULL
BEGIN
    INSERT INTO category (category_name, description, status) VALUES (N'Thiết bị IT', N'Máy tính, màn hình, linh kiện', 'ACTIVE');
    SET @CatID = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Laptop Dell XPS 15')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Laptop Dell XPS 15', N'Laptop cao cấp cho đồ họa', 'FIXED_ASSET', 'ACTIVE', 'STRAIGHT_LINE', 36, 'i7, 16GB, 512GB SSD', @CatID, 'XPS-15-2024');
END

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Màn hình Dell UltraSharp 27')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Màn hình Dell UltraSharp 27', N'Màn hình 4K chuyên dụng', 'EQUIPMENT', 'ACTIVE', 'STRAIGHT_LINE', 48, '27 inch, 4K, IPS', @CatID, 'U2723QE');
END

-- 3. Thông tin User 
DECLARE @AdminID INT, @DirectorID INT, @AssetMgrID INT, @StaffMgrID INT;
SELECT @AdminID = user_id FROM users WHERE username = 'admin';
SELECT @DirectorID = user_id FROM users WHERE username = 'director';
SELECT @AssetMgrID = user_id FROM users WHERE username = 'assetmanager';
SELECT @StaffMgrID = user_id FROM users WHERE username = 'purchasestaff';

-- Lấy Department mẫu (ID=1 thường là Admin/IT từ DataInitializer)
DECLARE @DeptID INT;
SELECT TOP 1 @DeptID = department_id FROM departments;

-- 4. Tạo Purchase Request (PR) - Trạng thái APPROVED
DECLARE @PR_ID INT;
INSERT INTO purchase_request (status, request_reason, note, creator_id, requesting_department_id, needed_by_date, priority, approved_by_director_id, approved_by_director_at, created_at)
VALUES ('APPROVED', N'Bổ sung thiết bị cho phòng IT', N'Gấp trong tháng 3', @AssetMgrID, @DeptID, DATEADD(DAY, 7, GETDATE()), 'HIGH', @DirectorID, GETDATE(), GETDATE());
SET @PR_ID = SCOPE_IDENTITY();

-- Detail PR
DECLARE @AT1_ID INT, @AT2_ID INT;
SELECT @AT1_ID = asset_type_id FROM asset_type WHERE type_name = N'Laptop Dell XPS 15';
SELECT @AT2_ID = asset_type_id FROM asset_type WHERE type_name = N'Màn hình Dell UltraSharp 27';

INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (35000000, 2, @PR_ID, @AT1_ID, N'Màu bạc');
DECLARE @PR_D1 INT = SCOPE_IDENTITY();

INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (12000000, 3, @PR_ID, @AT2_ID, N'Có chân đế xoay');
DECLARE @PR_D2 INT = SCOPE_IDENTITY();

-- 5. Tạo Quotation - Trạng thái APPROVED/SELECTED
DECLARE @QUO_ID INT;
DECLARE @SuppID INT;
SELECT @SuppID = supplier_id FROM supplier WHERE supplier_name = N'Phong Vũ IT';

INSERT INTO quotation (purchase_request_id, supplier_id, status, total_amount, created_at)
VALUES (@PR_ID, @SuppID, 'APPROVED', 106000000, GETDATE());
SET @QUO_ID = SCOPE_IDENTITY();

-- Detail Quotation
INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, quotation_detail_note, price, discount_rate, tax_rate, status)
VALUES (@QUO_ID, @PR_D1, @AT1_ID, 2, N'Hàng có sẵn', 34000000, 0, 10, 'APPROVED');
DECLARE @QD1_ID INT = SCOPE_IDENTITY();

INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, quotation_detail_note, price, discount_rate, tax_rate, status)
VALUES (@QUO_ID, @PR_D2, @AT2_ID, 3, N'Hàng có sẵn', 11500000, 0, 10, 'APPROVED');
DECLARE @QD2_ID INT = SCOPE_IDENTITY();

-- 6. Tạo Purchase Order (PO) - Trạng thái PENDING (Để Warehouse thấy và nhập kho)
DECLARE @PO_ID INT;
INSERT INTO purchase_orders (total_amount, note, status, created_at, purchase_request_id, supplier_id, quotation_id, approved_by)
VALUES (106000000, N'Đơn hàng mẫu test nhập kho', 'PENDING', GETDATE(), @PR_ID, @SuppID, @QUO_ID, @DirectorID);
SET @PO_ID = SCOPE_IDENTITY();

-- Detail PO
INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (2, 34000000, 10, @PO_ID, @AT1_ID, @QD1_ID, DATEADD(DAY, 2, GETDATE()));

INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (3, 11500000, 10, @PO_ID, @AT2_ID, @QD2_ID, DATEADD(DAY, 2, GETDATE()));

-- 7. Setup Kho và Zone
DECLARE @WH_ID INT;
SELECT @WH_ID = warehouse_id FROM wh_warehouses WHERE name = N'Main Warehouse';
IF @WH_ID IS NULL
BEGIN
    INSERT INTO wh_warehouses (name, address, manager_user_id, status)
    VALUES (N'Main Warehouse', N'123 Logistics St.', @AdminID, 'ACTIVE');
    SET @WH_ID = SCOPE_IDENTITY();
END

-- Zone cho Laptop
IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Zone IT - Laptop')
BEGIN
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Zone IT - Laptop', 50, 0, @AT1_ID, 'ACTIVE');
END

-- Zone cho Monitor
IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Zone IT - Monitor')
BEGIN
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Zone IT - Monitor', 30, 0, @AT2_ID, 'ACTIVE');
END

-- Zone Trống (Dùng chung)
IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'General Zone A')
BEGIN
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'General Zone A', 100, 0, NULL, 'ACTIVE');
END

PRINT 'Dummy data for PO Inbound testing created successfully.';
