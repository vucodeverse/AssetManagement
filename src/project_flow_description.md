# Tài Liệu Đặc Tả Luồng Nghiệp Vụ - Asset Management System

Tài liệu này cung cấp cái nhìn chi tiết về kiến trúc phần mềm, các quy trình nghiệp vụ và logic xử lý cốt lõi trong hệ thống quản lý tài sản.

---

## 1. Luồng Purchase Request (Yêu Cầu Mua Sắm - PR)

**Mô tả:** Quy trình bắt đầu khi một cá nhân/bộ phận cần tài sản mới.

### Các thành phần kỹ thuật:
- **Model:** `Purchase`, `PurchaseDetail`
- **Controller:** `PurchaseController`
- **Service:** `PurchaseServiceImpl`
- **Enum Trạng thái (`Request`):** `DRAFT`, `PENDING`, `APPROVED`, `REJECTED`
- **Vai trò thực hiện:** `DEPARTMENT_MANAGER` (Người tạo), `DIRECTOR` (Người duyệt)

### Quy trình chi tiết:
1.  **Khởi tạo PR:** Hệ thống tạo object `Purchase` thông qua `PurchaseMapper`.
2.  **Lưu trữ:**
    - **Draft:** Lưu yêu cầu với trạng thái `DRAFT` để chỉnh sửa sau.
    - **Submit:** Gửi yêu cầu với trạng thái `PENDING`.
3.  **Duyệt/Từ chối:**
    - `DIRECTOR` kiểm tra PR. Nếu đồng ý, trạng thái chuyển sang `APPROVED`. Nếu không, chuyển sang `REJECTED` kèm lý do (`rejectReason`).
    - Logic xử lý tại `PurchaseServiceImpl.actionsWithPurchase`.

---

## 2. Luồng Quotation (Báo Giá)

**Mô tả:** Sau khi PR được duyệt, nhân viên mua hàng thu thập báo giá từ các nhà cung cấp.

### Các thành phần kỹ thuật:
- **Model:** `Quotation`, `QuotationDetail`
- **Service:** `QuotationServiceImpl`
- **Enum Trạng thái:** `DRAFT`, `PENDING`, `APPROVED`, `REJECTED`, `SUBMITTED`
- **Vai trò thực hiện:** `PURCHASE_STAFF`

### Logic xử lý cốt lõi:
1.  **Mapping dữ liệu:** Dữ liệu từ `PurchaseDetail` được map sang `QuotationDetail` để đảm bảo tính nhất quán về loại tài sản và số lượng yêu cầu.
2.  **Tính toán tài chính (`calculateTotal`):**
    - `Subtotal = Quantity * Price`
    - `Discount = Subtotal * DiscountRate / 100`
    - `TaxableAmount = Subtotal - Discount`
    - `Tax = TaxableAmount * TaxRate / 100`
    - `LineTotal = TaxableAmount + Tax`
3.  **Ràng buộc:** Một PR có thể có nhiều báo giá từ các nhà cung cấp khác nhau (`purchase_id` làm khóa ngoại).

---

## 3. Luồng Purchase Order (Đơn Hàng - PO)

**Mô tả:** Chuyển đổi báo giá đã chọn thành đơn hàng chính thức.

### Các thành phần kỹ thuật:
- **Model:** `Order`, `OrderDetail`
- **Service:** `OrderServiceImpl`
- **Enum Trạng thái (`OrderStatus`):** `PENDING`, `APPROVED`, `REJECTED`, `CREATED`

### Quy trình kiểm tra nghiêm ngặt (Validation):
1.  **Kiểm tra số lượng (Crucial):**
    - Hệ thống tính toán: `Số lượng đã đặt (OrderedQty)` của dòng PR đó trong tất cả các PO trước.
    - Công thức: `Quantity_Mới + OrderedQty <= PR_Quantity`.
    - Nếu vi phạm, hệ thống ném `InvalidDataException` (ngăn chặn đặt hàng vượt quá nhu cầu duyệt).
2.  **Chốt giá:** Giá, thuế và chiết khấu tại thời điểm tạo PO được lấy trực tiếp từ `QuotationDetail` để tránh thay đổi dữ liệu (tampering).
3.  **Cập nhật trạng thái:** Khi PO được tạo thành công, báo giá (`Quotation`) liên quan sẽ tự động chuyển trạng thái thành `APPROVED`.

---

## 4. Hệ Thống Dashboard & Phân Quyền

### Dashboard Service (`DashboardServiceImpl`):
- Tổng hợp dữ liệu bằng SQL (COUNT, SUM) để đảm bảo hiệu năng.
- **KPIs:** Số lượng yêu cầu chờ duyệt, giá trị đơn hàng trong tháng, biểu đồ xu hướng (nếu có).
- **Recent Activity:** Hiển thị 5-10 bản ghi mới nhất từ các bảng PR, Quotation, PO.

### Phân quyền (Roles):
- `ADMIN`: Quản lý hệ thống, danh mục.
- `PURCHASE_STAFF`: Xử lý báo giá, liên hệ NCC.
- `DIRECTOR`: Phê duyệt PR và chọn báo giá để tạo PO.
- `DEPARTMENT_MANAGER`: Người đề xuất nhu cầu.

---

## 5. Công Nghệ Sử Dụng (Tech Stack Context)
- **Backend:** Spring Boot, Spring Data JDBC/DAO Pattern.
- **Mapping:** MapStruct (chuyển đổi DTO <-> Entity).
- **Frontend:** Thymeleaf template engine.
- **Validation:** Server-side validation với custom exceptions.
