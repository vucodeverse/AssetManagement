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
                    info: "SHOWING _START_ TO _END_ OF _TOTAL_ REQUESTS",
                    emptyTable: "No pending purchase requests."
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
                    info: "SHOWING _START_ TO _END_ OF _TOTAL_ QUOTATIONS",
                    emptyTable: "No quotations yet."
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
                    info: "SHOWING _START_ TO _END_ OF _TOTAL_ ORDERS",
                    emptyTable: "No active purchase orders."
                }
            })
        } catch (e) {
            console.error("Error initializing PO DataTable:", e);
        }
    }
});
