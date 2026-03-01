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
        window.location.href = '/director/purchase-orders';
    });
});
