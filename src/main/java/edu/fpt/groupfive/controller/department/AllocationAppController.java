package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.service.AllocationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/asset-manager/allocation-request")
public class AllocationAppController {
    private final AllocationRequestService allocationRequestService;

    @GetMapping("/listbyAssetMgr")
    public String showList2(Model model) {
        // Lấy toàn bộ dang sách yêu cầu cấp phát
        List<AllocationRequest> list = allocationRequestService.getAllAllocationRequest(2);

        model.addAttribute("requests", list);

        return "allocation/allocation_request_action";
    }

    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable("id") Integer id) {

        /*
         try {
            allocationRequestService.updateStatus(id, "APPROVED", 6, null); // 6 là id AM mẫu
            redirectAttributes.addFlashAttribute("message", "Duyệt yêu cầu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/asset-manager/allocation-request/listbyAssetMgr";
    }
         */

        allocationRequestService.updateStatus(
                id,
                "APPROVED",
                6, // id AM (sau này lấy từ session)
                null);

        return "redirect:/asset-manager/allocation-request/listbyAssetMgr";
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable("id") Integer id,
                                @RequestParam("reasonReject") String reasonReject) {

        /*
        try {
            allocationRequestService.updateStatus(id, "REJECTED", 6, reasonReject);
            redirectAttributes.addFlashAttribute("message", "Đã từ chối yêu cầu!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/asset-manager/allocation-request/listbyAssetMgr";
         */

        allocationRequestService.updateStatus(
                id,
                "REJECTED",
                6, // id AM
                reasonReject); // sau này có thể cho nhập reason

        return "redirect:/asset-manager/allocation-request/listbyAssetMgr";
    }
}
