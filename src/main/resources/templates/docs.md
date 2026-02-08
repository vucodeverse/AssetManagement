Dưới đây là tài liệu đặc tả tương tác View - Controller được viết bằng Markdown. Bạn có thể lưu nội dung này vào file `VIEW_INTERACTION.md`.

---

# View-Controller Interaction Specification

Tài liệu này mô tả cơ chế hoạt động của giao diện (View) `department.html` / `category.html` và cách nó tương tác với Controller thông qua các tham số URL. Hệ thống sử dụng mô hình **Server-Side Rendering (SSR)**, trong đó một View duy nhất xử lý cả 3 trạng thái: Tạo mới, Xem chi tiết và Chỉnh sửa.

## 1. Dữ liệu đầu vào (Data Model)

Khi Controller trả về View, nó sẽ gửi kèm một `Model` chứa các dữ liệu sau để Thymeleaf render:

| Tên Attribute | Kiểu dữ liệu | Mô tả |
| --- | --- | --- |
| `departmentPage` | `Page<Dto>` | **Dữ liệu bảng (List):** Danh sách các bản ghi hiện tại, bao gồm cả thông tin phân trang (`number`, `totalPages`). |
| `departmentDto` | `Dto Object` | **Dữ liệu form:** Object chứa thông tin chi tiết. Nếu là `create`, object này rỗng. Nếu `view/edit`, object này chứa data của bản ghi được chọn. |
| `managerList` | `List<Object>` | **Dữ liệu bổ trợ:** Danh sách dùng để hiển thị trong các dropdown (`<select>`). |
| `mode` | `String` | **Cờ trạng thái:** Quyết định giao diện Form sẽ hiển thị thế nào. Các giá trị: `create`, `view`, `edit`. |
| `search` | `String` | Giá trị tìm kiếm hiện tại (để giữ lại text trong ô input search sau khi reload). |
| `sortDir` | `String` | Hướng sắp xếp hiện tại (`asc` hoặc `desc`). |

---

## 2. Các trạng thái Giao diện (State Management)

Giao diện thay đổi trạng thái dựa hoàn toàn vào tham số `mode` và `id` trên URL.

### 2.1. Trạng thái: TẠO MỚI (Default)

Đây là trạng thái mặc định khi truy cập trang hoặc bấm nút "Reset".

* **URL Pattern:** `GET /departments` (hoặc `?mode=create`)
* **Dữ liệu nhận:** `departmentDto` rỗng (new Object).
* **Hành vi View:**
* **Form:** Các ô input trống.
* **Trạng thái:** Cho phép nhập liệu (Editable).
* **Nút bấm:** Hiển thị nút **"Tạo mới"** và **"Reset"**.



### 2.2. Trạng thái: XEM CHI TIẾT (Read-only)

Kích hoạt khi người dùng bấm vào icon "Mắt" <i class="far fa-eye"></i> trên bảng danh sách.

* **URL Pattern:** `GET /departments?id={id}&mode=view`
* **Dữ liệu nhận:** `departmentDto` chứa dữ liệu của bản ghi có `id` tương ứng.
* **Hành vi View:**
* **Form:** Fill dữ liệu vào các ô input.
* **Trạng thái:** **Read-only** (Input chuyển màu xám, không thể sửa).
* **Nút bấm:** Hiển thị nút **"Cập nhật"** (chuyển sang edit) và **"Xóa"**.



### 2.3. Trạng thái: CHỈNH SỬA (Edit)

Kích hoạt khi người dùng bấm nút "Cập nhật" từ trạng thái Xem chi tiết.

* **URL Pattern:** `GET /departments?id={id}&mode=edit`
* **Dữ liệu nhận:** `departmentDto` chứa dữ liệu của bản ghi.
* **Hành vi View:**
* **Form:** Fill dữ liệu vào các ô input.
* **Trạng thái:** Cho phép sửa (Editable).
* **Nút bấm:** Hiển thị nút **"Lưu"** và **"Hủy"** (quay lại chế độ view).



---

## 3. Tương tác Danh sách & Bộ lọc (List Actions)

Các thao tác trên bảng bên trái sẽ reload trang với các tham số `GET` mới để lọc dữ liệu, nhưng logic hiển thị Form bên phải vẫn tuân theo quy tắc ở mục 2.

| Hành động | Thay đổi URL Param | Mô tả hành vi |
| --- | --- | --- |
| **Search** | `?search=keyword` | Reload trang, bảng chỉ hiển thị các bản ghi khớp với từ khóa. |
| **Sort** | `?sortDir=desc&sortField=name` | Reload trang, bảng sắp xếp lại. Thẻ `<a>` trên header cột sẽ đảo ngược giá trị `sortDir` cho lần click tiếp theo. |
| **Phân trang** | `?page={number}` | Reload trang, hiển thị trang dữ liệu tiếp theo (Ví dụ: Trang 2, Trang 3). |

> **Lưu ý:** Các tham số này hoạt động cộng gộp. Ví dụ: `?page=1&search=IT&sortDir=asc` (Xem trang 2 của kết quả tìm kiếm "IT", sắp xếp tăng dần).

---

## 4. Luồng xử lý dữ liệu (POST Actions)

View sử dụng HTML Form để gửi dữ liệu lên Server. Thymeleaf sẽ render `action` URL khác nhau tùy theo nút bấm:

### 4.1. Tạo mới (Create)

* **Nút bấm:** "Tạo mới"
* **Route:** `POST /departments?add`
* **Payload:** Toàn bộ dữ liệu trong Form.
* **Kết quả:** Server tạo mới -> Redirect về trang danh sách (`/departments`).

### 4.2. Lưu cập nhật (Update)

* **Nút bấm:** "Lưu"
* **Route:** `POST /departments?save`
* **Payload:** Toàn bộ dữ liệu trong Form (bao gồm cả Hidden Field `id`).
* **Kết quả:** Server cập nhật DB -> Redirect về trang danh sách.

### 4.3. Xóa (Delete)

* **Nút bấm:** "Xóa" (Icon thùng rác hoặc nút đỏ)
* **Route:** `POST /departments/{id}`
* **Payload:** `id` được gửi qua Path Variable.
* **Kết quả:** Server xóa bản ghi -> Redirect về trang danh sách.

---

## 5. Tóm tắt Luồng đi (User Flow)

1. **User** vào `/departments` -> **View** hiện Form trống (`mode=create`).
2. **User** click 1 dòng -> URL đổi thành `?id=5&mode=view` -> **View** hiện Form có data ID 5 (chỉ xem).
3. **User** click "Cập nhật" -> URL đổi thành `?id=5&mode=edit` -> **View** cho phép sửa input.
4. **User** sửa và click "Lưu" -> **Form** POST về `/departments?save` -> **Controller** lưu và Redirect về bước 1.