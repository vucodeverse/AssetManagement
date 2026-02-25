$(document).ready(function () {
    function updateTotals() {
        let grandTotal = 0;
        $('.qty-input').each(function () {
            const qty = parseFloat($(this).val()) || 0;
            const price = parseFloat($(this).attr('data-price')) || 0;
            const tax = parseFloat($(this).attr('data-tax')) || 0;
            const disc = parseFloat($(this).attr('data-discount')) || 0;

            // subtotal = qty * price * (1 + tax% - disc%)
            grandTotal += qty * price * (1 + (tax / 100) - (disc / 100));
        });

        const formatted = '$' + Math.round(grandTotal).toLocaleString('en-US');
        $('#grandTotalDisplay').text(formatted);
        $('#totalAmountDisplay').val(formatted);
        $('#totalAmountInput').val(grandTotal.toFixed(2));
    }

    $('.qty-input').on('input', updateTotals);
});
