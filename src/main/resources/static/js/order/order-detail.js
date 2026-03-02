$(document).ready(function () {
    if ($('#lineItemsTable').length) {
        $('#lineItemsTable').DataTable({
            pageLength: 5,
            lengthChange: false,
            ordering: true,
            info: true,
            searching: false,
            order: [[1, "asc"]],
            language: {
                paginate: {
                    previous: "‹",
                    next: "›"
                },
                info: "Showing _START_ to _END_ of _TOTAL_ items"
            }
        });
    }
});
