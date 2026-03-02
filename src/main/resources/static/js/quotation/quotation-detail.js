$(document).ready(function () {
    if (!$('#quotationDetailTable').length) return;

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
            info: "SHOWING _START_ TO _END_ OF _TOTAL_ ENTRIES"
        }
    });
});
