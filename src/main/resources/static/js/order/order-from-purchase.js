$(document).ready(function () {

    $('#statusFilter, #supplierFilter, #amountRange, #dateFrom, #dateTo, #purchaseIdFilter').on('change', function () {
        $('#filterForm').submit();
    });

    $('#btnClearFilter').on('click', function () {
        window.location.href = '/purchase-orders';
    });


        $('#purchaseOrdersTable').DataTable({
            pageLength: 6,
            lengthChange: false,
            ordering: true,
            info: true,
            searching: false,
            order: [[6, "desc"]],
            columnDefs: [
                { orderable: false, targets: 4 }
            ],
            language: {
                paginate: { previous: "<", next: ">" },
                info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ ĐƠN ĐẶT HÀNG",
                emptyTable: "Không có đơn đặt hàng nào."
            }
        });
});
