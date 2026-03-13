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
