$(document).ready(function () {

    $('#quotationDetailTable').DataTable({
        pageLength: 5,
        lengthChange: false,
        ordering: true,
        info: true,
        searching: false,
        dom: 'rtp',
        order: [[0, 'asc']],
        columnDefs: [],
        language: {
            paginate: { previous: "<", next: ">" },
            info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ MỤC",
            emptyTable: "Không có dữ liệu."
        }
    });
});


