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
                    info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ YÊU CẦU MUA SẮM",
                    emptyTable: "Không có yêu cầu mua sắm nào."
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
                    info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ BÁO GIÁ",
                    emptyTable: "Không có báo giá nào."
                }
            })
        } catch (e) {
            console.error(e);
        }
    }

    // Generic clickable row handler
    document.addEventListener('click', function(e) {
        const row = e.target.closest('.clickable-row[data-href]');
        if (row) {
            window.location.href = row.getAttribute('data-href');
        }
    });
});