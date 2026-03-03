$(document).ready(function () {
    function calculateTotals() {
        let subtotal = 0;
        let totalTax = 0;

        $('.items-table tbody tr').each(function () {
            const qty = parseFloat($(this).find('.qty-input').val()) || 0;
            const price = parseFloat($(this).find('.price-input').val()) || 0;
            const taxRate = parseFloat($(this).find('.tax-input').val()) || 0;
            const discRate = parseFloat($(this).find('.disc-input').val()) || 0;

            const lineSubtotal = qty * price;
            const discount = lineSubtotal * (discRate / 100);
            const taxableAmount = lineSubtotal - discount;
            const tax = taxableAmount * (taxRate / 100);

            subtotal += lineSubtotal;
            totalTax += tax;
        });

        const grandTotal = subtotal + totalTax;

        $('#subtotalDisplay').text('$ ' + subtotal.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }));
        $('#taxDisplay').text('$ ' + totalTax.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }));
        $('#grandTotalDisplay').text('$ ' + grandTotal.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }));
    }

    $('input').on('input', calculateTotals);
    calculateTotals();
})