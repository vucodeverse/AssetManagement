let selectedIds = new Set();

$(document).ready(function () {
    const table = $('#quotationDetailTable').DataTable({
        pageLength: 5,
        lengthChange: false,
        ordering: true,
        info: true,
        searching: false,
        dom: 'rtp',
        order: [[3, "asc"]],
        columnDefs: [
            { orderable: false, targets: 5 }
        ],
        language: {
            paginate: { previous: "<", next: ">" },
            info: "HIỂN THỊ TỪ _START_ ĐẾN _END_ TRONG TỔNG SỐ _TOTAL_ MỤC",
            emptyTable: "Không có dữ liệu."
        }
    });

    // Khi bảng được vẽ lại (chuyển trang, lọc, v.v.), cập nhật trạng thái checkbox
    table.on('draw', function () {
        $('.cb-compare').each(function () {
            let id = $(this).val();
            $(this).prop('checked', selectedIds.has(id));
        });
        updateCheckAllState();
    });

    $('#tableSearch, #statusFilter, #supplierFilter, #amountFilter').change(function () {
        $('#filterForm').submit();
    });

    // Xử lý khi tick từng checkbox lẻ
    $('#quotationDetailTable').on('change', '.cb-compare', function () {
        let id = $(this).val();
        if ($(this).prop('checked')) {
            if (selectedIds.size >= 3) {
                $(this).prop('checked', false);
                alert("Vui lòng chỉ chọn tối đa 3 báo giá để so sánh tốt nhất.");
                return;
            }
            selectedIds.add(id);
        } else {
            selectedIds.delete(id);
        }
        updateCompareButton();
        updateCheckAllState();
    });

    // Xử lý khi tick Check All (chỉ tác động lên trang hiện tại)
    $('#quotationDetailTable').on('change', '#checkAllCompare', function () {
        let isChecked = $(this).prop('checked');
        $('.cb-compare').each(function () {
            let id = $(this).val();
            if (isChecked) {
                if (selectedIds.size < 3) {
                    selectedIds.add(id);
                    $(this).prop('checked', true);
                }
            } else {
                selectedIds.delete(id);
                $(this).prop('checked', false);
            }
        });
        updateCompareButton();
    });

    $('#btnCompare').click(function (e) {
        e.preventDefault();
        openCompareModal();
    });
});

function updateCheckAllState() {
    let allOnPage = $('.cb-compare').length;
    let checkedOnPage = $('.cb-compare:checked').length;
    $('#checkAllCompare').prop('checked', allOnPage > 0 && allOnPage === checkedOnPage);
}

function updateCompareButton() {
    let checkedCount = selectedIds.size;
    $('#compareCount').text(checkedCount);

    if (checkedCount >= 2 && checkedCount <= 3) {
        $('#btnCompare').prop('disabled', false).show();
    } else {
        $('#btnCompare').prop('disabled', true);
        if (checkedCount === 0) {
            $('#btnCompare').hide();
        } else {
            $('#btnCompare').show();
        }
    }
}

function openCompareModal() {
    if (selectedIds.size < 2 || selectedIds.size > 3) {
        alert("Vui lòng chọn từ 2 đến 3 báo giá để so sánh.");
        return;
    }

    let purchaseId = $('#purchaseIdForCompare').val();
    let ids = Array.from(selectedIds);

    let compareBaseUrl = $('#btnCompare').data('url');
    window.location.href = `${compareBaseUrl}?purchaseId=${purchaseId}&ids=${ids.join(',')}`;
}

function closeCompareModal() {
    $('#compareModal').css("display", "none");
}

function openRejectModal(e, btn) {
    if (e) e.stopPropagation();

    const action = $(btn).data("action");

    $('#rejectForm').attr("action", action);
    $('#rejectModal textarea').val("");
    $('#rejectModal').css("display", "block");

}

function closeRejectModal() {
    $('#rejectModal').css("display", "none");
}
