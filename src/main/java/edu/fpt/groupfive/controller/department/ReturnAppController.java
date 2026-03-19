package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.dto.response.ReturnRequestRespnse;
import edu.fpt.groupfive.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/asset-manager/return-request")
public class ReturnAppController {
    private final ReturnRequestService returnRequestService;

    @GetMapping("/listbyAssetMgr")
    public String showList(Model model) {
        // Lấy danh sách yêu cầu trả hàng (giả định departmentId = 1 hoặc null để lấy hết)
        List<ReturnRequestRespnse> list = returnRequestService.getAllRequest(1); 
        model.addAttribute("requests", list);
        return "return/return_request_action";
    }

    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable("id") Integer id) {
        returnRequestService.updateStatus(id, "APPROVED", 2); // Id AM cố định để test
        return "redirect:/asset-manager/return-request/listbyAssetMgr";
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable("id") Integer id) {
        returnRequestService.updateStatus(id, "REJECTED", 2);
        return "redirect:/asset-manager/return-request/listbyAssetMgr";
    }
}
