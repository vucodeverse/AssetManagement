$(document).ready(function() {
    const dtConfig = {
        "paging": false,         // Tắt tính năng phân trang
        "scrollY": "250px",      // Đặt chiều cao khối (250px tương đương với khoảng 5 dòng)
        "scrollCollapse": true,  // Tự động thu gọn khung nếu danh sách có ít hơn 5 tài sản
        "lengthChange": false,   // Ẩn tùy chọn chọn số lượng hiển thị
        "searching": false,      // Tắt thanh tìm kiếm
        "info": false,
        "language": {
            "emptyTable": "Không có tài sản nào"
        }
    };
    if($('#assetTable').length) {
        $('#assetTable').DataTable(dtConfig);
    }
    if($('#assetTableNew').length) {
        $('#assetTableNew').DataTable(dtConfig);
    }
});


function toggleRow(chk) {
    const row = chk.closest("tr");
    const noteInput = row.querySelector(".noteInput");
    noteInput.readOnly = !chk.checked;
    buildDetails();
}

function buildDetails() {
    const container = document.getElementById("detailsContainer");
    container.innerHTML = "";
    let index = 0;

    const tables = ["assetTable", "assetTableNew"];
    tables.forEach(id => {
        if ($.fn.DataTable.isDataTable('#' + id)) {
            const table = $('#' + id).DataTable();
            table.$('tr').each(function() {
                const chk = $(this).find('.chkAsset')[0];
                if (chk && chk.checked) {
                    const assetId = chk.value;
                    const note = $(this).find('.noteInput').val();
                    container.innerHTML += `
                                <input type="hidden" name="details[${index}].assetId" value="${assetId}">
                                <input type="hidden" name="details[${index}].note" value="${note}">
                            `;
                    index++;
                }
            });
        } else {
            const el = document.getElementById(id);
            if (el) {
                const rows = el.querySelectorAll("tbody tr");
                rows.forEach(row => {
                    const chk = row.querySelector(".chkAsset");
                    if (chk && chk.checked) {
                        const assetId = chk.value;
                        const note = row.querySelector(".noteInput").value;
                        container.innerHTML += `
                                    <input type="hidden" name="details[${index}].assetId" value="${assetId}">
                                    <input type="hidden" name="details[${index}].note" value="${note}">
                                `;
                        index++;
                    }
                });
            }
        }
    });
}
document.querySelector("form").addEventListener("submit", function () {
    buildDetails();
});