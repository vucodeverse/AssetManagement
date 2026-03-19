-- ======================================================
-- SCRIPT TẠO DỮ LIỆU MẪU: LUỒNG MUA SẮM -> NHẬP KHO
-- ======================================================

-- 1. Cơ sở hạ tầng: Phòng ban & Người dùng
INSERT INTO departments (department_name, status) VALUES (N'Phòng Hành chính', 'ACTIVE'); -- ID 1
INSERT INTO departments (department_name, status) VALUES (N'Phòng Kỹ thuật', 'ACTIVE');  -- ID 2

INSERT INTO users (username, password_hash, first_name, last_name, status, role, department_id)
VALUES ('director', 'hash', N'Nguyễn', N'Giám Đốc', 'ACTIVE', 'DIRECTOR', 1); -- ID 1
INSERT INTO users (username, password_hash, first_name, last_name, status, role, department_id)
VALUES ('wh_manager', 'hash', N'Trần', N'Thủ Kho', 'ACTIVE', 'WAREHOUSE_MANAGER', 1); -- ID 2

-- 2. Danh mục & Loại tài sản
INSERT INTO category (category_name, status) VALUES (N'Thiết bị điện tử', 'ACTIVE'); -- ID 1
INSERT INTO asset_type (type_name, type_class, status, category_id, model) 
VALUES (N'Laptop Dell Latitude', 'HARDWARE', 'ACTIVE', 1, 'L5420'); -- ID 1

-- Định mức dung lượng cho Laptop (1 unit = 1 volume)
INSERT INTO wh_asset_capacity (asset_type_id, unit_volume) VALUES (1, 1);

-- 3. Thiết lập Kho & Vị trí (Zones)
INSERT INTO wh_warehouses (name, address, manager_user_id, status)
VALUES (N'Kho Trung Tâm', N'Số 1 Trịnh Văn Bô', 2, 'ACTIVE'); -- ID 1

-- Tạo Zone trống (max 50 đơn vị thể tích)
INSERT INTO wh_zones (warehouse_id, zone_name, max_capacity, current_capacity, asset_type_id, status)
VALUES (1, N'Zone-A1', 50, 0, NULL, 'ACTIVE'); 

-- 4. Nhà cung cấp & Quy trình mua sắm (PO)
INSERT INTO supplier (supplier_name, phone_number, email, address, status)
VALUES (N'Dell Store VN', '0987654321', 'sales@dell.vn', N'TP.HCM', 'ACTIVE'); -- ID 1

-- Tạo Purchase Request (Giả định đã duyệt)
INSERT INTO purchase_request (status, request_reason, creator_id, requesting_department_id, priority, approved_by_director_id)
VALUES ('APPROVED', N'Mua mới cho nhân viên', 2, 2, 'HIGH', 1); -- ID 1

INSERT INTO purchase_request_detail (estimated_price, quantity, purchase_request_id, asset_type_id)
VALUES (20000000, 10, 1, 1); -- ID 1

-- Tạo Quotation (Giả định đã duyệt)
INSERT INTO quotation (purchase_request_id, supplier_id, status, total_amount)
VALUES (1, 1, 'APPROVED', 200000000); -- ID 1

INSERT INTO quotation_detail (quotation_id, purchase_request_detail_id, asset_type_id, quantity, price)
VALUES (1, 1, 1, 10, 20000000); -- ID 1

-- TẠO PURCHASE ORDER (Đây là đối tượng chính để Kho nhập)
-- Lưu ý: Trạng thái phải là PENDING để xuất hiện trong danh sách chờ nhập
INSERT INTO purchase_orders (total_amount, status, purchase_request_id, supplier_id, quotation_id, approved_by)
VALUES (200000000, 'PENDING', 1, 1, 1, 1); -- ID 1

INSERT INTO purchase_order_details (quantity, unit_price, purchase_order_id, asset_type_id, quotation_detail_id)
VALUES (10, 20000000, 1, 1, 1); -- ID 1

PRINT 'Dữ liệu mẫu đã sẵn sàng. Bạn có thể đăng nhập vào Kho để thấy PO-1 chờ nhập kho.';
