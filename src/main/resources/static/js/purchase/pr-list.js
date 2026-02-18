//auto filter trong dropdown
document.addEventListener("DOMContentLoaded", function () {

    // lấy form filter
    const form = document.getElementById("filterForm");
    if(!form) return;
    // tạo mảng id của status và pri rồi duyệt
    ["statusFilter", "priorityFilter"].forEach(id => {

        // lấy ra từng select
        const el = document.getElementById(id);
        if (el) {

            // gắn event
            el.addEventListener("change", function () {
                form.submit(); // dropdown đổi value sẽ tự submit
            });
        }
    });

});


// $ trong jQuery là biến đại diện cho object jQuery hay $ === jQuery
$(function (){
    $('#purchaseTO').DataTable({
        pageLength: 10,
        lengthChange: false,
        ordering: true,
        info: true,
        searching:false,
        order: [[5, "desc"]],
    // cấu hình theo từng cột
    // owqr đây ko cho cột số 6 dc phép order
    columnDefs: [
        {
            orderable:false,
            targets: 6
        }
    ],
        language: {
            paginate: { previous: "‹", next: "›" }
        }
    })

})
