    $(document).ready(function () {


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

        $('#priorityFilter, #amountRange, #from, #to').on('change', function () {
            console.log("Filter changed, submitting form...");
            $('#filterForm').submit();
        });

    });