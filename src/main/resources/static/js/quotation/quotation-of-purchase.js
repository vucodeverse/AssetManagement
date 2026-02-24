$(document).ready(function () {
    const table = $('#quotationDetailTable').DataTable({
        pageLength: 5,
        lengthChange: false,
        ordering: true,
        info: true,
        searching: false,
        dom: 'rtp',
        order: [[3, "asc"]],
        columnDefs: [
            { orderable: false, targets: 5 }
        ],
        language: {
            paginate: { previous: "<", next: ">" },
            info: "SHOWING _START_ TO _END_ OF _TOTAL_ ENTRIES"
        }
    });

    $('#tableSearch').on('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            $('#filterForm').submit();
        }
    });

    const filters = ["statusFilter", "supplierFilter", "amountFilter"];
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
