document.addEventListener("DOMContentLoaded", function () {

    let subtotal = 0;

    // Lấy tất cả row có data-qty
    const rows = document.querySelectorAll("tr[data-qty]");

    rows.forEach(row => {

        const qty = parseFloat(row.dataset.qty) || 0;
        const price = parseFloat(row.dataset.price) || 0;

        subtotal += qty * price;
    });

    // Format số theo kiểu VN
    const formatted = subtotal.toLocaleString('vi-VN');

    // Gán vào
    document.getElementById("subtotal").textContent = formatted;
});
