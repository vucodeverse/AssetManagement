document.addEventListener("DOMContentLoaded", function () {
    const tableEl = document.getElementById("purchaseTO");

    if (tableEl && typeof $ !== 'undefined' && typeof $.fn.DataTable !== 'undefined') {
        try {
            $(tableEl).DataTable({
                pageLength: 5,
                lengthChange: false,
                ordering: true,
                info: true,
                searching: false,
                order: [[1, "asc"], [5, "desc"]],
                columnDefs: [
                    { orderable: false, targets: 6 }
                ],
                language: {
                    paginate: { previous: "<", next: ">" },
                    info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ YÊU CẦU MUA SẮM",
                    emptyTable: "Không có yêu cầu mua sắm nào."
                }
            });
        } catch (e) {
            console.error(e);
        }
    }


    $('#statusFilter, #priorityFilter, #from, #to').change(function (){
        $('#filterForm').submit();
    });

});

function openRejectModal(e,btn) {
    if(e) e.stopPropagation();

    const action = $(btn).data("action"); // biến DOM btn thành object jquery và lấy giá trị ở data-action của html


    $('#rejectForm').attr("action", action); // attr dùng để đọc hoặc thay đổi attribute
    $("#rejectForm textarea").val(""); // xóa nội dung textarea
    $('#rejectModal').css("display", "flex"); // css cho form

}

function closeRejectModal() {
    $('#rejectModal').css("display", "none")
}
