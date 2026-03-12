$(document).ready(function () {
    if ($('#lineItemsTable').length) {
        $('#lineItemsTable').DataTable({
            pageLength: 5,
            lengthChange: false,
            ordering: true,
            info: true,
            searching: false,
            order: [[1, "asc"]],
            language: {
                paginate: {
                    previous: "‹",
                    next: "›"
                },
                info: "Hiển thị từ _START_ đến _END_ trong tổng số _TOTAL_ mục",
                emptyTable: "Không có dữ liệu."
            }
        });
    }
});
