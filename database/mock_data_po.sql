-- =========================================================================
-- MOCK DATA FOR TESTING WAREHOUSE INBOUND PO FLOW
-- File: database/mock_data_po.sql
-- =========================================================================

-- 1. Đảm bảo có Category và Asset Type
IF NOT EXISTS (SELECT 1 FROM category WHERE category_name = N'Thiết bị CNTT')
BEGIN
    INSERT INTO category (category_name, description, status) 
    VALUES (N'Thiết bị CNTT', N'Máy tính, màn hình, phụ kiện', 'ACTIVE');
END

DECLARE @CategoryID INT = (SELECT TOP 1 category_id FROM category WHERE category_name = N'Thiết bị CNTT');

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Laptop Dell XPS 15')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, category_id, model)
    VALUES (N'Laptop Dell XPS 15', N'Laptop văn phòng cao cấp', 'HARDWARE', 'ACTIVE', @CategoryID, 'XPS-15-2024');
END
DECLARE @TypeLaptopID INT = (SELECT TOP 1 asset_type_id FROM asset_type WHERE type_name = N'Laptop Dell XPS 15');

IF NOT EXISTS (SELECT 1 FROM asset_type WHERE type_name = N'Màn hình Dell 27 inch')
BEGIN
    INSERT INTO asset_type (type_name, description, type_class, status, category_id, model)
    VALUES (N'Màn hình Dell 27 inch', N'Màn hình độ phân giải 4K', 'HARDWARE', 'ACTIVE', @CategoryID, 'U2723QE');
END
DECLARE @TypeMonitorID INT = (SELECT TOP 1 asset_type_id FROM asset_type WHERE type_name = N'Màn hình Dell 27 inch');

-- 2. Tạo Nhà cung cấp (Supplier)
IF NOT EXISTS (SELECT 1 FROM supplier WHERE supplier_name = N'HPT Technology')
BEGIN
    INSERT INTO supplier (supplier_name, phone_number, email, address, supplier_code, status)
    VALUES (N'HPT Technology', '028-3820-2222', 'contact@hpt.vn', N'Quận 1, TP.HCM', 'SUP-HPT-001', 'ACTIVE');
END
DECLARE @SupplierID INT = (SELECT TOP 1 supplier_id FROM supplier WHERE supplier_name = N'HPT Technology');

-- 3. Tạo Yêu cầu mua sắm (Purchase Request)
-- Giả sử ID người dùng 1 là Admin/Manager (Bạn hãy kiểm tra bảng users nếu cần)
DECLARE @UserID INT = 1; 

INSERT INTO purchase_request (status, request_reason, note, creator_id, needed_by_date, priority, created_at)
VALUES ('APPROVED', N'Bổ sung thiết bị cho nhân viên mới đợt tháng 4', N'Ưu tiên Laptop', @UserID, DATEADD(day, 10, GETDATE()), 'HIGH', GETDATE());

DECLARE @PR_ID INT = SCOPE_IDENTITY();

-- Chi tiết PR
INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id, spec_requirement)
VALUES (35000000, 2, @PR_ID, @TypeLaptopID, N'Core i7, 32GB RAM'),
       (8000000, 3, @PR_ID, @TypeMonitorID, N'27 inch, 4K');

DECLARE @PRD_Laptop_ID INT = (SELECT TOP 1 purchase_request_detail_id FROM purchase_request_detail WHERE purchase_request_id = @PR_ID AND asset_type_id = @TypeLaptopID);
DECLARE @PRD_Monitor_ID INT = (SELECT TOP 1 purchase_request_detail_id FROM purchase_request_detail WHERE purchase_request_id = @PR_ID AND asset_type_id = @TypeMonitorID);

-- 4. Tạo Báo giá (Quotation)
INSERT INTO quotation (purchase_request_id, supplier_id, status, total_amount, created_at)
VALUES (@PR_ID, @SupplierID, 'SELECTED', 94000000, GETDATE());

DECLARE @QuotationID INT = SCOPE_IDENTITY();

INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price, discount_rate, tax_rate)
VALUES (@QuotationID, @PRD_Laptop_ID, @TypeLaptopID, 2, 34500000, 0, 10),
       (@QuotationID, @PRD_Monitor_ID, @TypeMonitorID, 3, 7800000, 0, 10);

DECLARE @QD_Laptop_ID INT = (SELECT TOP 1 quotation_detail_id FROM quotation_detail WHERE quotation_id = @QuotationID AND asset_type_id = @TypeLaptopID);

-- 5. Tạo Đơn mua hàng (Purchase Order) - Đây là dữ liệu then chốt cho luồng Inbound
INSERT INTO purchase_orders (total_amount, note, status, purchase_request_id, supplier_id, quotation_id, created_at)
VALUES (94000000, N'Nhập kho test luồng Warehouse', 'PENDING', @PR_ID, @SupplierID, @QuotationID, GETDATE());

DECLARE @PO_ID INT = SCOPE_IDENTITY();

INSERT INTO purchase_order_details (quantity, unit_price, tax_rate, purchase_order_id, asset_type_id, quotation_detail_id)
VALUES (2, 34500000, 10, @PO_ID, @TypeLaptopID, @QD_Laptop_ID),
       (3, 7800000, 10, @PO_ID, @TypeMonitorID, NULL);

-- 6. Setup Kho (Warehouse & Zone) để có chỗ chứa
IF NOT EXISTS (SELECT 1 FROM wh_warehouses WHERE name = N'Kho Tổng FPT')
BEGIN
    INSERT INTO wh_warehouses (name, address, manager_user_id, status)
    VALUES (N'Kho Tổng FPT', N'Khu Công nghệ cao, Quận 9', @UserID, 'ACTIVE');
END
DECLARE @WarehouseID INT = (SELECT TOP 1 warehouse_id FROM wh_warehouses WHERE name = N'Kho Tổng FPT');

IF NOT EXISTS (SELECT 1 FROM wh_zones WHERE zone_name = N'Khu A1 - Laptop')
BEGIN
    INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, status)
    VALUES (@WarehouseID, N'Khu A1 - Laptop', 50, 0, 'ACTIVE');
END

-- Đảm bảo có định mức sức chứa
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @TypeLaptopID)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@TypeLaptopID, 1);
IF NOT EXISTS (SELECT 1 FROM wh_asset_capacity WHERE asset_type_id = @TypeMonitorID)
    INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (@TypeMonitorID, 2);

-- KẾT THÚC: Dữ liệu đã sẵn sàng để hiển thị tại /wh/inbound/po
SELECT 'PO Created ID: ' + CAST(@PO_ID AS VARCHAR) AS Result;
