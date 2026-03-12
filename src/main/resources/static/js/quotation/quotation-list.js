$(document).ready(function () {
    const $form = $('#filterForm');

    $('#priorityFilter, #amountRange, input[name="from"], input[name="to"]').on('change', function () {
        if ($form.length) {
            $form.submit();
        }
    });

    if ($('#quotation-table').length) {
        $('#quotation-table').DataTable({
            pageLength: 6,
            lengthChange: false,
            ordering: true,
            info: true,
            searching: false,
            order: [[1, "desc"]],
            columnDefs: [
                { orderable: false, targets: 5 }
            ],
            language: {
                paginate: {
                    previous: "‹",
                    next: "›"
                }
            }
        });
    }
});