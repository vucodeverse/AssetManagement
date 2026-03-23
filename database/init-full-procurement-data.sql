-- =============================================
-- FULL DUMMY DATA FOR PROCUREMENT FLOW TESTING
-- =============================================


-- 1. Thêm Nhà cung cấp (Suppliers)
IF NOT EXISTS (SELECT 1 FROM supplier WHERE supplier_name = N'Phong Vũ IT')
    INSERT INTO supplier (supplier_name, phone_number, email, address, supplier_code, tax_code, status)
    VALUES (N'Phong Vũ IT', '028111222', 'contact@phongvu.vn', N'264 Nguyễn Thị Minh Khai, Q3, HCM', 'SUP001', 'TAX001', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM supplier WHERE supplier_name = N'Hòa Phát Furniture')
    INSERT INTO supplier (supplier_name, phone_number, email, address, supplier_code, tax_code, status)
    VALUES (N'Hòa Phát Furniture', '028333444', 'sales@hoaphat.vn', N'120 Nguyễn Thái Học, Q1, HCM', 'SUP002', 'TAX002', 'ACTIVE');

-- 2. Thêm Danh mục (Categories)
DECLARE @CatIT INT, @CatFur INT, @CatNet INT;

IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = N'Thiết bị IT')
BEGIN
    INSERT INTO category (category_name, description, status) VALUES (N'Thiết bị IT', N'Máy tính, laptop, linh kiện', 'ACTIVE');
    SET @CatIT = SCOPE_IDENTITY();
END ELSE SELECT @CatIT = category_id FROM category WHERE category_name = N'Thiết bị IT';

IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = N'Nội thất văn phòng')
BEGIN
    INSERT INTO category (category_name, description, status) VALUES (N'Nội thất văn phòng', N'Bàn, ghế, tủ văn phòng', 'ACTIVE');
    SET @CatFur = SCOPE_IDENTITY();
END ELSE SELECT @CatFur = category_id FROM category WHERE category_name = N'Nội thất văn phòng';

IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = N'Thiết bị Mạng')
BEGIN
    INSERT INTO category (category_name, description, status) VALUES (N'Thiết bị Mạng', N'Switch, Router, Access Point', 'ACTIVE');
    SET @CatNet = SCOPE_IDENTITY();
END ELSE SELECT @CatNet = category_id FROM category WHERE category_name = N'Thiết bị Mạng';

-- 3. Thêm Loại tài sản (Asset Types)
DECLARE @AT_MBP INT, @AT_DELL INT, @AT_AERON INT, @AT_DESK INT, @AT_CISCO INT, @AT_UBI INT, @AT_PRINTER INT, @AT_PHOTOCOPY INT;

-- IT
IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'MacBook Pro 14 M3')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'MacBook Pro 14 M3', N'Laptop Apple 2024', 'FIXED_ASSET', 'ACTIVE', 'STRAIGHT_LINE', 36, 'M3, 16GB, 512GB', @CatIT, 'MBP14-M3');
    SET @AT_MBP = SCOPE_IDENTITY();
END ELSE SELECT @AT_MBP = asset_type_id FROM asset_type WHERE type_name = N'MacBook Pro 14 M3';

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Dell Precision 3660')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Dell Precision 3660', N'Máy trạm cấu hình cao', 'FIXED_ASSET', 'ACTIVE', 'STRAIGHT_LINE', 48, 'i9, 64GB, RTX A2000', @CatIT, 'P3660-WS');
    SET @AT_DELL = SCOPE_IDENTITY();
END ELSE SELECT @AT_DELL = asset_type_id FROM asset_type WHERE type_name = N'Dell Precision 3660';

-- Furniture
IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Herman Miller Aeron')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Herman Miller Aeron', N'Ghế công thái học cao cấp', 'EQUIPMENT', 'ACTIVE', 'STRAIGHT_LINE', 60, 'Size B, Pellicle Mesh', @CatFur, 'HM-AERON-V2');
    SET @AT_AERON = SCOPE_IDENTITY();
END ELSE SELECT @AT_AERON = asset_type_id FROM asset_type WHERE type_name = N'Herman Miller Aeron';

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Standing Desk 140x70')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Standing Desk 140x70', N'Bàn làm việc thay đổi chiều cao', 'EQUIPMENT', 'ACTIVE', 'STRAIGHT_LINE', 48, 'Dual Motor, Wood Top', @CatFur, 'SD-14070');
    SET @AT_DESK = SCOPE_IDENTITY();
END ELSE SELECT @AT_DESK = asset_type_id FROM asset_type WHERE type_name = N'Standing Desk 140x70';

-- Networking
IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Cisco Switch 2960')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Cisco Switch 2960', N'Switch layer 2 manager', 'FIXED_ASSET', 'ACTIVE', 'STRAIGHT_LINE', 60, '24 Port Gigabit, POE+', @CatNet, 'C2960-24P');
    SET @AT_CISCO = SCOPE_IDENTITY();
END ELSE SELECT @AT_CISCO = asset_type_id FROM asset_type WHERE type_name = N'Cisco Switch 2960';

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Ubiquiti AP AC Pro')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Ubiquiti AP AC Pro', N'Access Point Wifi chuyên dụng', 'EQUIPMENT', 'ACTIVE', 'STRAIGHT_LINE', 36, '802.11ac, Dual-Band', @CatNet, 'UAP-AC-PRO');
    SET @AT_UBI = SCOPE_IDENTITY();
END ELSE SELECT @AT_UBI = asset_type_id FROM asset_type WHERE type_name = N'Ubiquiti AP AC Pro';

-- Office Equipment
IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Máy in HP')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Máy in HP', N'Máy in laser đa chức năng', 'EQUIPMENT', 'ACTIVE', 'STRAIGHT_LINE', 36, 'In, Scan, Copy', @CatIT, 'HP-LaserJet-Pro');
    SET @AT_PRINTER = SCOPE_IDENTITY();
END ELSE SELECT @AT_PRINTER = asset_type_id FROM asset_type WHERE type_name = N'Máy in HP';

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Máy photocopy')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, default_depreciation_method, default_useful_life_months, specification, category_id, model)
    VALUES (N'Máy photocopy', N'Máy photocopy công suất lớn', 'FIXED_ASSET', 'ACTIVE', 'STRAIGHT_LINE', 60, 'In A3, A4, Scan mạng', @CatIT, 'Ricoh-Aficio');
    SET @AT_PHOTOCOPY = SCOPE_IDENTITY();
END ELSE SELECT @AT_PHOTOCOPY = asset_type_id FROM asset_type WHERE type_name = N'Máy photocopy';

-- 4. Thông tin User & Department
DECLARE @AdminID INT, @DirectorID INT, @AssetMgrID INT, @StaffMgrID INT, @DeptID INT;
SELECT @AdminID = user_id FROM users WHERE username = 'admin';
SELECT @DirectorID = user_id FROM users WHERE username = 'director';
SELECT @AssetMgrID = user_id FROM users WHERE username = 'assetmanager';
SELECT @StaffMgrID = user_id FROM users WHERE username = 'purchasestaff';
SELECT TOP 1 @DeptID = department_id FROM departments;

-- 5. PHIÊN MUA SẮM 1: Đơn hàng IT (Phong Vũ)
DECLARE @PR1 INT, @QUO1 INT, @PO1 INT, @Supp1 INT;
SELECT @Supp1 = supplier_id FROM supplier WHERE supplier_name = N'Phong Vũ IT';

-- PR 1
INSERT INTO purchase_request (status, request_reason, note, creator_id, needed_by_date, priority, approved_by_director_id, approved_by_director_at, created_at)
VALUES ('APPROVED', N'Trang bị máy tính cho nhân viên mới', N'Cần gấp trong tuần này', @AssetMgrID, DATEADD(DAY, 7, GETDATE()), 'CRITICAL', @DirectorID, GETDATE(), GETDATE());
SET @PR1 = SCOPE_IDENTITY();

-- PR details 1
INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (45000000, 5, @PR1, @AT_MBP, N'16GB RAM tối thiểu');
DECLARE @PRD1_1 INT = SCOPE_IDENTITY();
INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (35000000, 2, @PR1, @AT_DELL, N'Card đồ họa rời');
DECLARE @PRD1_2 INT = SCOPE_IDENTITY();

-- Quotation 1
INSERT INTO quotation (purchase_request_id, supplier_id, status, total_amount, created_at)
VALUES (@PR1, @Supp1, 'APPROVED', 295000000, GETDATE());
SET @QUO1 = SCOPE_IDENTITY();

-- Quotation details 1
INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price, tax_rate, status)
VALUES (@QUO1, @PRD1_1, @AT_MBP, 5, 44000000, 10, 'APPROVED');
DECLARE @QD1_1 INT = SCOPE_IDENTITY();
INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price, tax_rate, status)
VALUES (@QUO1, @PRD1_2, @AT_DELL, 2, 34500000, 10, 'APPROVED');
DECLARE @QD1_2 INT = SCOPE_IDENTITY();

-- PO 1 (PENDING for Inbound)
INSERT INTO purchase_orders (total_amount, note, status, created_at, purchase_request_id, supplier_id, quotation_id, approved_by)
VALUES (295000000, N'PO Mua máy tính thiết kế', 'PENDING', GETDATE(), @PR1, @Supp1, @QUO1, @DirectorID);
SET @PO1 = SCOPE_IDENTITY();

INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (5, 44000000, 10, @PO1, @AT_MBP, @QD1_1, DATEADD(DAY, 3, GETDATE()));
INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (2, 34500000, 10, @PO1, @AT_DELL, @QD1_2, DATEADD(DAY, 3, GETDATE()));


-- 6. PHIÊN MUA SẮM 2: Đơn hàng Nội thất (Hòa Phát)
DECLARE @PR2 INT, @QUO2 INT, @PO2 INT, @Supp2 INT;
SELECT @Supp2 = supplier_id FROM supplier WHERE supplier_name = N'Hòa Phát Furniture';

-- PR 2
INSERT INTO purchase_request (status, request_reason, note, creator_id, needed_by_date, priority, approved_by_director_id, approved_by_director_at, created_at)
VALUES ('APPROVED', N'Setup lại khu vực làm việc lầu 2', N'Theo phong cách hiện đại', @AssetMgrID, DATEADD(DAY, 7, GETDATE()), 'MEDIUM', @DirectorID, GETDATE(), GETDATE());
SET @PR2 = SCOPE_IDENTITY();

-- PR details 2
INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (25000000, 8, @PR2, @AT_AERON, N'Size B');
DECLARE @PRD2_1 INT = SCOPE_IDENTITY();
INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (7000000, 8, @PR2, @AT_DESK, N'Màu sồi');
DECLARE @PRD2_2 INT = SCOPE_IDENTITY();

-- Quotation 2
INSERT INTO quotation (purchase_request_id, supplier_id, status, total_amount, created_at)
VALUES (@PR2, @Supp2, 'APPROVED', 256000000, GETDATE());
SET @QUO2 = SCOPE_IDENTITY();

-- Quotation details 2
INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price, tax_rate, status)
VALUES (@QUO2, @PRD2_1, @AT_AERON, 8, 24500000, 10, 'APPROVED');
DECLARE @QD2_1 INT = SCOPE_IDENTITY();
INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price, tax_rate, status)
VALUES (@QUO2, @PRD2_2, @AT_DESK, 8, 7000000, 10, 'APPROVED');
DECLARE @QD2_2 INT = SCOPE_IDENTITY();

-- PO 2 (PENDING for Inbound)
INSERT INTO purchase_orders (total_amount, note, status, created_at, purchase_request_id, supplier_id, quotation_id, approved_by)
VALUES (256000000, N'Đơn hàng nội thất văn phòng', 'PENDING', GETDATE(), @PR2, @Supp2, @QUO2, @DirectorID);
SET @PO2 = SCOPE_IDENTITY();

INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (8, 24500000, 10, @PO2, @AT_AERON, @QD2_1, DATEADD(DAY, 5, GETDATE()));
INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (8, 7000000, 10, @PO2, @AT_DESK, @QD2_2, DATEADD(DAY, 5, GETDATE()));


-- 7. PHIÊN MUA SẮM 3: Đơn hàng hỗn hợp (Phong Vũ)
DECLARE @PR3 INT, @QUO3 INT, @PO3 INT;

-- PR 3
INSERT INTO purchase_request (status, request_reason, note, creator_id, needed_by_date, priority, approved_by_director_id, approved_by_director_at, created_at)
VALUES ('APPROVED', N'Nâng cấp hạ tầng mạng & Server', N'Phục vụ dự án mới', @AssetMgrID, DATEADD(DAY, 7, GETDATE()), 'HIGH', @DirectorID, GETDATE(), GETDATE());
SET @PR3 = SCOPE_IDENTITY();

-- PR details 3
INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (15000000, 2, @PR3, @AT_CISCO, N'Chịu tải tốt');
DECLARE @PRD3_1 INT = SCOPE_IDENTITY();
INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (4000000, 4, @PR3, @AT_UBI, N'Chuẩn AC');
DECLARE @PRD3_2 INT = SCOPE_IDENTITY();

-- Quotation 3
INSERT INTO quotation (purchase_request_id, supplier_id, status, total_amount, created_at)
VALUES (@PR3, @Supp1, 'APPROVED', 46000000, GETDATE());
SET @QUO3 = SCOPE_IDENTITY();

-- Quotation details 3
INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price, tax_rate, status)
VALUES (@QUO3, @PRD3_1, @AT_CISCO, 2, 14500000, 10, 'APPROVED');
DECLARE @QD3_1 INT = SCOPE_IDENTITY();
INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price, tax_rate, status)
VALUES (@QUO3, @PRD3_2, @AT_UBI, 4, 3800000, 10, 'APPROVED');
DECLARE @QD3_2 INT = SCOPE_IDENTITY();

-- PO 3 (PENDING for Inbound)
INSERT INTO purchase_orders (total_amount, note, status, created_at, purchase_request_id, supplier_id, quotation_id, approved_by)
VALUES (46000000, N'Hạ tầng mạng tháng 3', 'PENDING', GETDATE(), @PR3, @Supp1, @QUO3, @DirectorID);
SET @PO3 = SCOPE_IDENTITY();

INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (2, 14500000, 10, @PO3, @AT_CISCO, @QD3_1, DATEADD(DAY, 2, GETDATE()));
INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id, delivery_date)
VALUES (4, 3800000, 10, @PO3, @AT_UBI, @QD3_2, DATEADD(DAY, 2, GETDATE()));


-- 8. Setup Kho (WAREHOUSE & ZONES)
DECLARE @WH_ID INT;
SELECT @WH_ID = warehouse_id FROM wh_warehouses WHERE name = N'Main Warehouse';
IF @WH_ID IS NULL
BEGIN
    INSERT INTO wh_warehouses (name, address, manager_user_id, status)
    VALUES (N'Main Warehouse', N'123 Logistics St.', @AdminID, 'ACTIVE');
    SET @WH_ID = SCOPE_IDENTITY();
END

-- 8.1. Định mức thể tích của từng loại tài sản (wh_asset_capacity)
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @AT_MBP)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@AT_MBP, 1);
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @AT_DELL)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@AT_DELL, 2);
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @AT_AERON)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@AT_AERON, 5);
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @AT_DESK)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@AT_DESK, 8);
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @AT_CISCO)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@AT_CISCO, 3);
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @AT_UBI)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@AT_UBI, 1);
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @AT_PRINTER)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@AT_PRINTER, 5);
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @AT_PHOTOCOPY)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@AT_PHOTOCOPY, 15);

-- 8.2. Quản lý Khu vực lưu trữ (Dynamic Zones)
-- Zone cho Laptop
IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Khu Laptop - Kệ A1')
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Khu Laptop - Kệ A1', 100, 0, @AT_MBP, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Khu Laptop - Kệ B1')
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Khu Laptop - Kệ B1', 100, 0, @AT_DELL, 'ACTIVE');

-- Zone cho Furniture
IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Khu Nội thất - Tầng 2')
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Khu Nội thất - Tầng 2', 50, 0, @AT_AERON, 'ACTIVE');

-- Zone cho Networking
IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Phòng Rack - Tủ A')
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Phòng Rack - Tủ A', 30, 0, @AT_CISCO, 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Phòng Rack - Tủ B')
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Phòng Rack - Tủ B', 30, 0, @AT_UBI, 'ACTIVE');

-- Zone cho Office Equipment (Printers/Photocopiers)
IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Khu Thiết bị văn phòng - Hàng B1')
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Khu Thiết bị văn phòng - Hàng B1', 150, 0, @AT_PRINTER, 'ACTIVE');

-- Zone chung cho tài sản khác
IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Khu vực chờ phân loại - Floor 1')
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
    VALUES (@WH_ID, N'Khu vực chờ phân loại - Floor 1', 200, 0, NULL, 'ACTIVE');

PRINT 'Full Procurement dummy data created successfully.';

-- 12.1. Sinh ra một số Asset đang nằm trong kho (AVAILABLE)
DECLARE @AssetPrinter1 INT = 20;
DECLARE @AssetPhoto1 INT = 21;

SET IDENTITY_INSERT asset ON;
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 20)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, original_cost, acquisition_date)
    VALUES (20, N'Máy in HP Phòng Đào Tạo 1', @AT_PRINTER, 'AVAILABLE', 4500000, GETDATE());

IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 21)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, original_cost, acquisition_date)
    VALUES (21, N'Máy photocopy Phòng Đào Tạo 1', @AT_PHOTOCOPY, 'AVAILABLE', 25000000, GETDATE());
SET IDENTITY_INSERT asset OFF;

-- Đưa vào Zone (wh_asset_placement)
IF NOT EXISTS (SELECT 1 FROM wh_asset_placement WHERE asset_id = 20)
    INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at)
    SELECT @AssetPrinter1, zone_id, @AssetMgrID, GETDATE() FROM wh_zones WHERE zone_name = N'Khu Thiết bị văn phòng - Hàng B1';

IF NOT EXISTS (SELECT 1 FROM wh_asset_placement WHERE asset_id = 21)
    INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at)
    SELECT @AssetPhoto1, zone_id, @AssetMgrID, GETDATE() FROM wh_zones WHERE zone_name = N'Khu Thiết bị văn phòng - Hàng B1';

-- 12.2. Tạo Allocation Request đã được APPROVED (Mock ID: 101)
DECLARE @AllocReq1 INT = 101;

SET IDENTITY_INSERT allocation_request ON;
IF NOT EXISTS (SELECT 1 FROM allocation_request WHERE request_id = 101)
    INSERT INTO allocation_request (request_id, requester_id, requested_department_id, request_date, needed_by_date, priority, reason, status, am_approved_by, am_approved_at)
    VALUES (101, @StaffMgrID, @DeptID, GETDATE(), DATEADD(DAY, 3, GETDATE()), 'HIGH', N'Cấp phát thiết bị cho giảng viên mới', 'APPROVED', @AssetMgrID, GETDATE());
SET IDENTITY_INSERT allocation_request OFF;

-- Tạo Allocation Request Detail
IF NOT EXISTS (SELECT 1 FROM allocation_request_detail WHERE request_id = 101 AND asset_type_id = @AT_PRINTER)
    INSERT INTO allocation_request_detail (request_id, asset_type_id, quantity_requested, note)
    VALUES (101, @AT_PRINTER, 1, N'Cần loại in đa năng');

IF NOT EXISTS (SELECT 1 FROM allocation_request_detail WHERE request_id = 101 AND asset_type_id = @AT_PHOTOCOPY)
    INSERT INTO allocation_request_detail (request_id, asset_type_id, quantity_requested, note)
    VALUES (101, @AT_PHOTOCOPY, 1, N'Cần máy photo công suất lớn');

-- 12.3. Tạo Asset Handover (Lệnh xuất kho) ở trạng thái PENDING (Mock ID: 601)
DECLARE @Handover1 INT = 601;

SET IDENTITY_INSERT asset_handover ON;
IF NOT EXISTS (SELECT 1 FROM asset_handover WHERE handover_id = 601)
    INSERT INTO asset_handover (handover_id, handover_type, allocation_request_id, to_department_id, executed_by_user_id, status, note, created_at)
    VALUES (601, 'ALLOCATION', 101, @DeptID, @AdminID, 'PENDING', N'Phiếu xuất kho cấp phát (MOCK ID 601)', GETDATE());
SET IDENTITY_INSERT asset_handover OFF;

PRINT 'Outbound Allocation mock data created successfully.';

-- =============================================
-- 13. BỔ SUNG THÊM DỮ LIỆU CẤP PHÁT & TÀI SẢN KHO (EXTRA DATA)
-- =============================================

-- 13.1. Thêm thêm Phòng ban
DECLARE @DeptKT INT, @DeptHC INT;

IF NOT EXISTS (SELECT 1 FROM departments WHERE department_name = N'Phòng Kỹ thuật')
BEGIN
    INSERT INTO departments (department_name, status, description) 
    VALUES (N'Phòng Kỹ thuật', 'ACTIVE', N'Phòng phát triển phần mềm và hạ tầng');
    SET @DeptKT = SCOPE_IDENTITY();
END ELSE SELECT @DeptKT = department_id FROM departments WHERE department_name = N'Phòng Kỹ thuật';

IF NOT EXISTS (SELECT 1 FROM departments WHERE department_name = N'Phòng Hành chính')
BEGIN
    INSERT INTO departments (department_name, status, description) 
    VALUES (N'Phòng Hành chính', 'ACTIVE', N'Quản lý nhân sự và văn phòng phẩm');
    SET @DeptHC = SCOPE_IDENTITY();
END ELSE SELECT @DeptHC = department_id FROM departments WHERE department_name = N'Phòng Hành chính';

-- 13.2. Sinh thêm Asset đang nằm trong kho (AVAILABLE)
-- Lấy lại các biến ID cần thiết (vì DECLARE ở trên có thể khác batch)
DECLARE @Extra_AdminID INT, @Extra_AssetMgrID INT;
SELECT @Extra_AdminID = user_id FROM users WHERE username = 'admin';
SELECT @Extra_AssetMgrID = user_id FROM users WHERE username = 'assetmanager';

DECLARE @Extra_AT_MBP INT, @Extra_AT_DELL INT, @Extra_AT_CISCO INT;
SELECT @Extra_AT_MBP = asset_type_id FROM asset_type WHERE type_name = N'MacBook Pro 14 M3';
SELECT @Extra_AT_DELL = asset_type_id FROM asset_type WHERE type_name = N'Dell Precision 3660';
SELECT @Extra_AT_CISCO = asset_type_id FROM asset_type WHERE type_name = N'Cisco Switch 2960';

SET IDENTITY_INSERT asset ON;
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 30)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, original_cost, acquisition_date)
    VALUES (30, N'MacBook Pro 14 M3 - WH 01', @Extra_AT_MBP, 'AVAILABLE', 44000000, GETDATE());
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 31)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, original_cost, acquisition_date)
    VALUES (31, N'MacBook Pro 14 M3 - WH 02', @Extra_AT_MBP, 'AVAILABLE', 44000000, GETDATE());
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 32)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, original_cost, acquisition_date)
    VALUES (32, N'Dell Precision 3660 - WH 01', @Extra_AT_DELL, 'AVAILABLE', 34500000, GETDATE());
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 33)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, original_cost, acquisition_date)
    VALUES (33, N'Cisco Switch 2960 - WH 01', @Extra_AT_CISCO, 'AVAILABLE', 14500000, GETDATE());
SET IDENTITY_INSERT asset OFF;

-- Đưa vào Zone
IF NOT EXISTS (SELECT 1 FROM wh_asset_placement WHERE asset_id = 30)
    INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at) 
    SELECT 30, zone_id, @Extra_AssetMgrID, GETDATE() FROM wh_zones WHERE zone_name = N'Khu Laptop - Kệ A1';

IF NOT EXISTS (SELECT 1 FROM wh_asset_placement WHERE asset_id = 31)
    INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at) 
    SELECT 31, zone_id, @Extra_AssetMgrID, GETDATE() FROM wh_zones WHERE zone_name = N'Khu Laptop - Kệ A1';

IF NOT EXISTS (SELECT 1 FROM wh_asset_placement WHERE asset_id = 32)
    INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at) 
    SELECT 32, zone_id, @Extra_AssetMgrID, GETDATE() FROM wh_zones WHERE zone_name = N'Khu Laptop - Kệ B1';

IF NOT EXISTS (SELECT 1 FROM wh_asset_placement WHERE asset_id = 33)
    INSERT INTO wh_asset_placement (asset_id, zone_id, placed_by, placed_at) 
    SELECT 33, zone_id, @Extra_AssetMgrID, GETDATE() FROM wh_zones WHERE zone_name = N'Phòng Rack - Tủ A';

-- 13.3. Tạo thêm Allocation Requests APPROVED
SET IDENTITY_INSERT allocation_request ON;
IF NOT EXISTS (SELECT 1 FROM allocation_request WHERE request_id = 102)
    INSERT INTO allocation_request (request_id, requester_id, requested_department_id, request_date, needed_by_date, priority, reason, status, am_approved_by, am_approved_at)
    VALUES (102, @Extra_AdminID, @DeptKT, GETDATE(), DATEADD(DAY, 5, GETDATE()), 'CRITICAL', N'Cấp phát laptop cho team dự án AI', 'APPROVED', @Extra_AssetMgrID, GETDATE());

IF NOT EXISTS (SELECT 1 FROM allocation_request WHERE request_id = 103)
    INSERT INTO allocation_request (request_id, requester_id, requested_department_id, request_date, needed_by_date, priority, reason, status, am_approved_by, am_approved_at)
    VALUES (103, @Extra_AdminID, @DeptHC, GETDATE(), DATEADD(DAY, 10, GETDATE()), 'MEDIUM', N'Cấp phát switch cho văn phòng mới', 'APPROVED', @Extra_AssetMgrID, GETDATE());
SET IDENTITY_INSERT allocation_request OFF;

-- Allocation Details
IF NOT EXISTS (SELECT 1 FROM allocation_request_detail WHERE request_id = 102 AND asset_type_id = @Extra_AT_MBP)
    INSERT INTO allocation_request_detail (request_id, asset_type_id, quantity_requested, note)
    VALUES (102, @Extra_AT_MBP, 2, N'Yêu cầu cấu hình M3 16GB');

IF NOT EXISTS (SELECT 1 FROM allocation_request_detail WHERE request_id = 103 AND asset_type_id = @Extra_AT_CISCO)
    INSERT INTO allocation_request_detail (request_id, asset_type_id, quantity_requested, note)
    VALUES (103, @Extra_AT_CISCO, 1, N'Cần loại 24 port');

-- 13.4. Tạo Asset Handovers PENDING
SET IDENTITY_INSERT asset_handover ON;
IF NOT EXISTS (SELECT 1 FROM asset_handover WHERE handover_id = 602)
    INSERT INTO asset_handover (handover_id, handover_type, allocation_request_id, to_department_id, executed_by_user_id, status, note, created_at)
    VALUES (602, 'ALLOCATION', 102, @DeptKT, @Extra_AdminID, 'PENDING', N'Phiếu xuất kho cho dự án AI', GETDATE());

IF NOT EXISTS (SELECT 1 FROM asset_handover WHERE handover_id = 603)
    INSERT INTO asset_handover (handover_id, handover_type, allocation_request_id, to_department_id, executed_by_user_id, status, note, created_at)
    VALUES (603, 'ALLOCATION', 103, @DeptHC, @Extra_AdminID, 'PENDING', N'Xuất kho thiết bị mạng cho VP lầu 3', GETDATE());
SET IDENTITY_INSERT asset_handover OFF;

PRINT 'All extra dummy data created successfully.';


-- =============================================
-- 14. PHIÊN THU HỒI TÀI SẢN (RETURN FLOW)
-- =============================================
PRINT 'Adding Asset Return flow dummy data...';

DECLARE @Ret_AdminUser INT, @Ret_KTDept INT, @Ret_HCDept INT;
DECLARE @Ret_TypeMBP INT, @Ret_TypeDell INT, @Ret_TypeAeron INT;

SELECT @Ret_AdminUser = user_id FROM users WHERE username = 'admin';
SELECT @Ret_KTDept = department_id FROM departments WHERE department_name = N'Phòng Kỹ thuật';
SELECT @Ret_HCDept = department_id FROM departments WHERE department_name = N'Phòng Hành chính';
SELECT @Ret_TypeMBP = asset_type_id FROM asset_type WHERE type_name = N'MacBook Pro 14 M3';
SELECT @Ret_TypeDell = asset_type_id FROM asset_type WHERE type_name = N'Dell Precision 3660';
SELECT @Ret_TypeAeron = asset_type_id FROM asset_type WHERE type_name = N'Herman Miller Aeron';

-- 14.1. Tạo tài sản đang được sử dụng (IN_USE) để thu hồi
SET IDENTITY_INSERT asset ON;

-- Tài sản cho Phòng Kỹ thuật (Sẽ thu hồi khác loại)
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 1001)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, department_id, original_cost, acquisition_date)
    VALUES (1001, N'MacBook Pro 14 M3 - KT 01', @Ret_TypeMBP, 'ASSIGNED', @Ret_KTDept, 44000000, DATEADD(MONTH, -10, GETDATE()));
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 1002)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, department_id, original_cost, acquisition_date)
    VALUES (1002, N'Dell Precision 3660 - KT 01', @Ret_TypeDell, 'ASSIGNED', @Ret_KTDept, 34500000, DATEADD(MONTH, -10, GETDATE()));

-- Tài sản cho Phòng Hành chính (Sẽ thu hồi cùng loại)
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 1003)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, department_id, original_cost, acquisition_date)
    VALUES (1003, N'Ghế Aeron - HC 01', @Ret_TypeAeron, 'ASSIGNED', @Ret_HCDept, 24500000, DATEADD(YEAR, -2, GETDATE()));
IF NOT EXISTS (SELECT 1 FROM asset WHERE asset_id = 1004)
    INSERT INTO asset (asset_id, asset_name, asset_type_id, current_status, department_id, original_cost, acquisition_date)
    VALUES (1004, N'Ghế Aeron - HC 02', @Ret_TypeAeron, 'ASSIGNED', @Ret_HCDept, 24500000, DATEADD(YEAR, -2, GETDATE()));

SET IDENTITY_INSERT asset OFF;

-- 14.2. Tạo Yêu cầu thu hồi (Return Request)
DECLARE @Ret_RR1 INT, @Ret_RR2 INT;

-- Request 1: Kỹ thuật trả Laptop + Workstation (Khác loại)
INSERT INTO return_request (requester_id, requested_department_id, request_date, reason, status, created_at)
VALUES (@Ret_AdminUser, @Ret_KTDept, GETDATE(), N'Dự án AI kết thúc, hoàn trả thiết bị cấu hình cao', 'APPROVED', GETDATE());
SET @Ret_RR1 = SCOPE_IDENTITY();

INSERT INTO return_request_detail (request_id, asset_id, note) VALUES (@Ret_RR1, 1001, N'Cần vệ sinh máy');
INSERT INTO return_request_detail (request_id, asset_id, note) VALUES (@Ret_RR1, 1002, N'Lỗi phím esc');

-- Request 2: Hành chính trả 2 ghế (Cùng loại)
INSERT INTO return_request (requester_id, requested_department_id, request_date, reason, status, created_at)
VALUES (@Ret_AdminUser, @Ret_HCDept, GETDATE(), N'Thay đổi layout văn phòng, thừa thiết bị', 'APPROVED', GETDATE());
SET @Ret_RR2 = SCOPE_IDENTITY();

INSERT INTO return_request_detail (request_id, asset_id, note) VALUES (@Ret_RR2, 1003, NULL);
INSERT INTO return_request_detail (request_id, asset_id, note) VALUES (@Ret_RR2, 1004, NULL);

-- 14.3. Tạo Lệnh bàn giao (Asset Handover - RETURN)
DECLARE @Ret_Handover1 INT, @Ret_Handover2 INT;

-- Handover 1 cho Request 1
INSERT INTO asset_handover (handover_type, return_request_id, from_department_id, to_department_id, executed_by_user_id, status, note, created_at)
VALUES ('RETURN', @Ret_RR1, @Ret_KTDept, NULL, @Ret_AdminUser, 'PENDING', N'Lệnh thu hồi thiết bị KT - 1001,1002', GETDATE());
SET @Ret_Handover1 = SCOPE_IDENTITY();

INSERT INTO asset_handover_detail (handover_id, asset_id, note) VALUES (@Ret_Handover1, 1001, N'Cần vệ sinh');
INSERT INTO asset_handover_detail (handover_id, asset_id, note) VALUES (@Ret_Handover1, 1002, N'Lỗi phím esc');

-- Handover 2 cho Request 2
INSERT INTO asset_handover (handover_type, return_request_id, from_department_id, to_department_id, executed_by_user_id, status, note, created_at)
VALUES ('RETURN', @Ret_RR2, @Ret_HCDept, NULL, @Ret_AdminUser, 'PENDING', N'Lệnh thu hồi nội thất HC - 1003,1004', GETDATE());
SET @Ret_Handover2 = SCOPE_IDENTITY();

INSERT INTO asset_handover_detail (handover_id, asset_id, note) VALUES (@Ret_Handover2, 1003, NULL);
INSERT INTO asset_handover_detail (handover_id, asset_id, note) VALUES (@Ret_Handover2, 1004, NULL);

-- 14.4. Một lệnh đã hoàn tất (APPROVED)
DECLARE @Ret_RR3 INT, @Ret_Handover3 INT;
INSERT INTO return_request (requester_id, requested_department_id, request_date, reason, status, created_at)
VALUES (@Ret_AdminUser, @Ret_KTDept, DATEADD(DAY, -1, GETDATE()), N'Hoàn trả thiết bị hỏng định kỳ', 'APPROVED', DATEADD(DAY, -1, GETDATE()));
SET @Ret_RR3 = SCOPE_IDENTITY();

-- Thêm chi tiết cho RR3 (Asset 30 - Máy in HP từ mẫu gốc đã có sẵn)
INSERT INTO return_request_detail (request_id, asset_id, note) VALUES (@Ret_RR3, 30, N'Hỏng đầu phun');

INSERT INTO asset_handover (handover_type, return_request_id, from_department_id, to_department_id, executed_by_user_id, status, note, created_at, updated_at)
VALUES ('RETURN', @Ret_RR3, @Ret_KTDept, NULL, @Ret_AdminUser, 'COMPLETED', N'Đã nhập kho hoàn thành', DATEADD(DAY, -1, GETDATE()), GETDATE());
SET @Ret_Handover3 = SCOPE_IDENTITY();

INSERT INTO asset_handover_detail (handover_id, asset_id, note) VALUES (@Ret_Handover3, 30, N'Hỏng đầu phun');

PRINT 'Processed return sample data added.';

