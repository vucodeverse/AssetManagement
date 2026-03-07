document.addEventListener("DOMContentLoaded", function () {
    const tablePR = document.getElementById("purchaseDB");
    const tableQO = document.getElementById("quotationDB");

    if (tablePR && typeof $ !== 'undefined' && typeof $.fn.DataTable !== 'undefined') {
        try {
            $(tablePR).DataTable({
                pageLength: 6,
                lengthChange: false,
                ordering: true,
                info: true,
                searching: false,
                order: [[5, "desc"]],
                language: {
                    paginate: { previous: "<", next: ">" },
                    info: "SHOWING _START_ TO _END_ OF _TOTAL_ PURCHASE REQUESTS"
                }
            })
        } catch (e) {
            console.error(e);
        }
    }

    if (tableQO && typeof $ !== 'undefined' && typeof $.fn.DataTable !== 'undefined') {
        try {
            $(tableQO).DataTable({
                pageLength: 6,
                lengthChange: false,
                ordering: true,
                info: true,
                searching: false,
                order: [[0, "desc"]],
                language: {
                    paginate: { previous: "<", next: ">" },
                    info: "SHOWING _START_ TO _END_ OF _TOTAL_ QUOTATIONS"
                }
            })
        } catch (e) {
            console.error(e);
        }
    }
});