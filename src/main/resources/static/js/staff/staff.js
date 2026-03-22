document.addEventListener("DOMContentLoaded", function () {
    const tablePR = document.getElementById("staff-pr-table");
    const tableQO = document.getElementById("staff-quo-table");
    const tablePO = document.getElementById("staff-po-table");

    if (tablePR && typeof $ !== 'undefined' && typeof $.fn.DataTable !== 'undefined') {
        try {
            $(tablePR).DataTable({
                pageLength: 4,
                lengthChange: false,
                ordering: true,
                info: true,
                searching: false,
                order: [[1, "asc"]],
                language: {
                    paginate: { previous: "<", next: ">" },
                    info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ YÊU CẦU",
                    emptyTable: "Không có yêu cầu mua sắm đang chờ xử lý."
                }
            })
        } catch (e) {
            console.error("Error initializing PR DataTable:", e);
        }
    }

    if (tableQO && typeof $ !== 'undefined' && typeof $.fn.DataTable !== 'undefined') {
        try {
            $(tableQO).DataTable({
                pageLength: 4,
                lengthChange: false,
                ordering: true,
                info: true,
                searching: false,
                order: [[1, "asc"]],
                language: {
                    paginate: { previous: "<", next: ">" },
                    info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ BÁO GIÁ",
                    emptyTable: "Chưa có báo giá nào."
                }
            })
        } catch (e) {
            console.error("Error initializing Quotation DataTable:", e);
        }
    }

    if (tablePO && typeof $ !== 'undefined' && typeof $.fn.DataTable !== 'undefined') {
        try {
            $(tablePO).DataTable({
                pageLength: 4,
                lengthChange: false,
                ordering: true,
                info: true,
                searching: false,
                order: [[0, "desc"]],
                language: {
                    paginate: { previous: "<", next: ">" },
                    info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ ĐƠN ĐẶT HÀNG",
                    emptyTable: "Không có đơn đặt hàng nào đang hoạt động."
                }
            })
        } catch (e) {
            console.error("Error initializing PO DataTable:", e);
        }
    }
});
