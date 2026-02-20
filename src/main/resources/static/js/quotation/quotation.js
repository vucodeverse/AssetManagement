// filter
document.addEventListener("DOMContentLoaded", function (){
    const form = document.getElementById(filterForm)
    if(!form) return;

    // laays ra mangr filter
    ["priorityFilter", "amountRange"].forEach(id =>{

        // lay tưng giá trị
        const el = document.getElementById(id);
        if(el){

            // gawns event va submit
            el.addEventListener("change", function () {
                form.submit();
            })
        }

        const from = form.querySelector('input[name="from"]');
        const to = form.querySelector('input[name="to"]');

        [from, to].forEach(el =>{
            if(!el) return;
            el.addEventListener("change", () => form.submit());
        })
    })
})

// auto sort
$(function (){
    $(#quotation-table).DataTable({
        pageLength: 6,
        lengthChange: false,
        ordering: true,
        info: true,
        search: false,
        order: [[1, "desc"]],
        columnDefs: [
            {
                orderable:false,
                targets: 5
            }
        ],
        language: {
            paginate: { previous: "‹", next: "›" }
        },
    })
})