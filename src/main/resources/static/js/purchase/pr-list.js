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
                order: [[5, "desc"]],
                columnDefs: [
                    { orderable: false, targets: 6 }
                ],
                language: {
                    paginate: { previous: "<", next: ">" },
                    info: "SHOWING _START_ TO _END_ OF _TOTAL_ PURCHASE REQUESTS"
                }
            });
        } catch (e) {
            console.error(e);
        }
    }

    const filters = ["statusFilter", "priorityFilter", "from", "to"];
    filters.forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener("change", function () {
                const form = document.getElementById("filterForm");
                if (form) form.submit();
            });
        }
    });
});
