document.addEventListener("DOMContentLoaded", function () {

    let subtotal = 0;

    const rows = document.querySelectorAll("tr[data-qty]");

    rows.forEach(row => {
        const qty = parseFloat(row.dataset.qty) || 0;
        const price = parseFloat(row.dataset.price) || 0;
        subtotal += qty * price;
    });

    const fmt = (n) => n.toLocaleString('vi-VN', { minimumFractionDigits: 0, maximumFractionDigits: 0 });

    const subtotalEl = document.getElementById("subtotal");
    if (subtotalEl) subtotalEl.textContent = fmt(subtotal);

    const totalEl = document.getElementById("total");
    if (totalEl) totalEl.textContent = fmt(subtotal);
});
