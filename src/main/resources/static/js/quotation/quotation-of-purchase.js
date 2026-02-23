$(document).ready(function () {
    const table = $('#quotationDetailTable').DataTable({
        pageLength: 5,
        lengthChange: false,
        ordering: true,
        info: false,
        searching: true,
        dom: 'rtp',
        order: [[3, "asc"]],
        columnDefs: [
            { orderable: false, targets: 5 },
            { searchable: true, targets: [0, 1] },
            { searchable: false, targets: [2, 3, 4, 5] }
        ],
        language: { paginate: { previous: "<", next: ">" } }
    });

    $('#tableSearch').on('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            table.search(this.value).draw();
        }
    });

    // Các bộ lọc bổ sung
    $.fn.dataTable.ext.search.push(function (settings, data) {
        if (settings.nTable.id !== 'quotationDetailTable') return true;

        const filterStatus = $('#statusFilter').val()?.toLowerCase().trim();
        const filterSupplier = $('#supplierFilter').val()?.toLowerCase().trim();
        const amountRange = $('#amountFilter').val();

        // Lấy dữ liệu từ bảng và làm sạch
        const rowStatus = (data[4] || "").toLowerCase().trim();
        const rowSupplier = (data[1] || "").toLowerCase().trim();

        // Lấy số tiền: Tìm chuỗi số đầu tiên trong ô (để loại bỏ các chữ như LOWEST)
        const rowAmountText = (data[3] || "").replace(/,/g, '').match(/\d+/)?.[0] || "0";
        const rowAmount = parseFloat(rowAmountText) || 0;

        // Kiểm tra khớp Status (Dùng includes vì ô có thể chứa icon/tag)
        const statusMatch = !filterStatus || rowStatus.includes(filterStatus);

        // Kiểm tra khớp Supplier (Dùng includes vì ô chứa cả tên NCC và nhãn Standard/Lowest)
        const supplierMatch = !filterSupplier || rowSupplier.includes(filterSupplier);

        // Kiểm tra khớp Khoảng giá
        let amountMatch = true;
        if (amountRange && amountRange !== "All" && amountRange !== "") {
            if (amountRange.includes('+')) {
                const min = parseFloat(amountRange.replace('+', '')) || 0;
                amountMatch = rowAmount >= min;
            } else {
                const parts = amountRange.split('-');
                if (parts.length === 2) {
                    const min = parseFloat(parts[0]) || 0;
                    const max = parseFloat(parts[1]) || 999999999;
                    amountMatch = rowAmount >= min && rowAmount <= max;
                }
            }
        }

        return statusMatch && supplierMatch && amountMatch;
    });

    $('#statusFilter, #supplierFilter, #amountFilter').on('change', () => table.draw());
});
