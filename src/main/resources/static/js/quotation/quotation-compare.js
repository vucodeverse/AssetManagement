$(document).ready(function() {
    // File JS riêng cho màn hình So sánh Báo giá (Full-page)
    console.log("Quotation Compare JS loaded");
});

function confirmAction(message, form) {
    if (confirm(message)) {
        form.submit();
    }
}
