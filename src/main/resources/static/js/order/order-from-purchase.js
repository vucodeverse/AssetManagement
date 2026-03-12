$(document).ready(function () {
    const $form = $('#filterForm');

    // Auto-submit khi đổi dropdown hoặc date
    $('#statusFilter, #supplierFilter, #amountRange, #dateFrom, #dateTo').on('change', function () {
        $form.submit();
    });

    // Search khi nhấn Enter trên input keyword
    $('#keyword').on('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            $form.submit();
        }
    });

    // Xóa tất cả các bộ lọc
    $('#btnClearFilter').on('click', function () {
        window.location.href = '/purchase-staff/purchase-orders';
    });

    // Initialize DataTable for PO List
    if ($('#purchaseOrdersTable').length) {
        $('#purchaseOrdersTable').DataTable({
            pageLength: 6,
            lengthChange: false,
            ordering: true,
            info: true,
            searching: false,
            order: [[1, "desc"]], // Sort by CREATED AT (index 1) descending by default
            columnDefs: [
                { orderable: false, targets: 4 } // Disable sorting on NOTE column
            ],
            language: {
                paginate: { previous: "<", next: ">" },
                info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ ĐƠN ĐẶT HÀNG",
                emptyTable: "Không có đơn đặt hàng nào."
            }
        });
    }
});
