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
            info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ MỤC",
            emptyTable: "Không có dữ liệu."
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

function openRejectModal(e, btn) {
    if (e) e.stopPropagation();

    const modal = document.getElementById('rejectModal');
    const form = document.getElementById('rejectForm');

    if (form && btn) form.action = btn.dataset.action;
    if (form) form.querySelector('textarea[name="reason"]').value = '';
    if (modal) modal.style.display = 'block';
}

function closeRejectModal() {
    const modal = document.getElementById('rejectModal');
    if (modal) modal.style.display = 'none';
}

window.onclick = function (event) {
    const modal = document.getElementById('rejectModal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
}
