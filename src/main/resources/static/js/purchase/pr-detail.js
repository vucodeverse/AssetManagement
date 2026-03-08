document.addEventListener("DOMContentLoaded", function () {

    let subtotal = 0;

    const rows = document.querySelectorAll("tr[data-qty]");

    rows.forEach(row => {
        const qty = parseFloat(row.dataset.qty) || 0;
        const price = parseFloat(row.dataset.price) || 0;

        subtotal += qty * price;
    });

    // format decimal(9,2) + dấu chấm ngăn cách
    const formatMoney = (num) => {
        return num.toLocaleString('vi-VN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    };

    document.getElementById("subtotal").textContent = formatMoney(subtotal);
    document.getElementById("total").textContent = formatMoney(subtotal);

});

function openRejectModal(btn) {
    const action = btn.dataset.action;
    const modal = document.getElementById("rejectModal");
    const form = document.getElementById("rejectForm");

    form.action = action;
    form.querySelector("textarea").value = "";
    modal.style.display = "flex";
}

function closeRejectModal() {
    document.getElementById("rejectModal").style.display = "none";
}
