# Mô Tả Luồng Nghiệp Vụ Dự Án Asset Management

Tài liệu này mô tả chi tiết các luồng nghiệp vụ chính trong hệ thống quản lý tài sản, bao gồm cách thức hoạt động và các thành phần mã nguồn liên quan.

---

## 1. Luồng Purchase Request (Yêu Cầu Mua Sắm)

**Mô tả:** Đây là bước khởi đầu khi một bộ phận hoặc cá nhân cần mua sắm tài sản mới.

### Các thành phần chính:
- **Controller:** `PurchaseController.java`
- **Service:** `PurchaseService.java`, `PurchaseServiceImpl.java`
- **DAO:** `PurchaseDAO.java`, `PurchaseDetailDAO.java`
- **Giao diện:** `purchase-form.html`, `purchase-detail.html`
- **JavaScript:** `pr-detail.js`, `pr-list.js`

### Quy trình chạy:
1. **Khởi tạo:** Người dùng truy cập `/asset-manager/purchase-form`. Hệ thống khởi tạo một object `PurchaseCreateRequest` với ít nhất một dòng chi tiết trống.
2. **Thao tác Form:**
   - Người dùng có thể thêm/xóa các dòng chi tiết tài sản (Asset Type, số lượng, yêu cầu kỹ thuật) thông qua các nút "Thêm dòng" hoặc "Xóa".
   - JavaScript xử lý việc cập nhật index cho các dòng chi tiết để Spring Boot có thể binding dữ liệu chính xác.
3. **Gửi yêu cầu:** Người dùng có thể chọn:
   - **Lưu nháp (Draft):** Trạng thái yêu cầu là `DRAFT`.
   - **Gửi duyệt (Pending):** Trạng thái yêu cầu là `PENDING`.
4. **Xử lý Backend:** `PurchaseServiceImpl` nhận DTO, map sang entity `Purchase` và `PurchaseDetail`, sau đó thực hiện lưu vào cơ sở dữ liệu.

---

## 2. Luồng Quotation (Báo Giá)

**Mô tả:** Sau khi Purchase Request được duyệt, nhân viên mua hàng (Purchase Staff) sẽ liên hệ các nhà cung cấp để lấy báo giá.

### Các thành phần chính:
- **Controller:** `QuotationController.java`
- **Service:** `QuotationService.java`, `QuotationServiceImpl.java`
- **DAO:** `QuotationDAO.java`, `QuotationDetailDAO.java`
- **Giao diện:** `quotation-form.html`, `quotation-detail.html`, `quotation-of-purchase.html`

### Quy trình chạy:
1. **Tạo báo giá:** Từ một PR đã duyệt, nhân viên chọn tạo báo giá. Hệ thống tự động sao chép danh sách tài sản từ PR sang form báo giá (`QuotationCreateRequest`).
2. **Nhập thông tin:** Nhân viên chọn nhà cung cấp và nhập giá cơ sở, thuế (Tax), chiết khấu (Discount) cho từng dòng sản phẩm.
3. **Tính toán:** Backend thực hiện tính toán `TotalAmount` cho từng dòng và tổng toàn bộ báo giá (bao gồm cả thuế và mức giảm giá).
4. **Lưu trữ:** Dữ liệu báo giá được lưu liên kết chặt chẽ với ID của Purchase Request gốc thông qua field `purchase_id`.

---

## 3. Luồng Purchase Order (Đơn Hàng)

**Mô tả:** Giám đốc (Director) hoặc người có thẩm quyền sẽ chọn một hoặc nhiều báo giá tốt nhất để tạo Đơn hàng chính thức.

### Các thành phần chính:
- **Controller:** `OrderController.java`
- **Service:** `OrderService.java`, `OrderServiceImpl.java`
- **DAO:** `OrderDAO.java`, `OrderDetailDAO.java`
- **Giao diện:** `order-from-purchase.html`, `order-form.html`, `order-of-purchase.html`

### Quy trình chạy:
1. **Chọn báo giá:** Người dùng xem danh sách các báo giá của một PR và chọn "Tạo PO" cho báo giá muốn thực hiện.
2. **Xác nhận số lượng:** Hệ thống tải thông tin từ báo giá lên form PO.
3. **Kiểm tra nghiệp vụ (Quan trọng):**
   - Backend kiểm tra số lượng đặt hàng trong PO không được vượt quá số lượng yêu cầu ban đầu trong PR.
   - Hệ thống tính toán tổng số lượng đã đặt ở các PO trước đó để xác định số lượng còn lại khả dụng.
4. **Phê duyệt:** PO sau khi tạo thường ở trạng thái `PENDING` chờ bước xử lý tiếp theo (nhận hàng/thanh toán).

---

## 4. Dashboard (Bảng Điều Khiển)

**Mô tả:** Cung cấp cái nhìn tổng quan về tình hình mua sắm và các yêu cầu cần xử lý gấp.

### Các thành phần chính:
- **Controller:** `DirectorController.java` (endpoint `/dashboard`)
- **Service:** `DashboardService.java`, `DashboardServiceImpl.java`
- **DTO:** `DashboardDTO.java`
- **Giao diện:** `director-dashboard.html`

### Dữ liệu hiển thị:
1. **KPI Cards:** Hiển thị số lượng PR đang chờ duyệt, Quotation đang chờ, tổng số PO đã tạo và tổng giá trị đơn hàng.
2. **Recent Activity:**
   - **Recent PRs:** Danh sách 5 yêu cầu mua sắm mới nhất.
   - **Recent Quotations:** Danh sách 5 báo giá vừa cập nhật.
3. **Luồng dữ liệu:** Dữ liệu được tổng hợp trực tiếp từ các DAO (Purchase, Quotation, Order) thông qua các câu lệnh COUNT và SUM trong SQL để đảm bảo tính thời gian thực.
