package edu.fpt.groupfive.controller.transfer;

import edu.fpt.groupfive.dto.request.search.AssetSearchCriteria;
import edu.fpt.groupfive.dto.request.transfer.TransferRequestCreate;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.dto.response.TransferResponse;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.DepartmentService;
import edu.fpt.groupfive.service.ITransferRequestService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.service.impl.TransferRequestDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/transfer-requests")
@RequiredArgsConstructor
public class TransferRequestController {

    private final ITransferRequestService transferRequestService;
    private final DepartmentService departmentService;
    private final AssetService assetService;
    private final UserService userService;
    private final TransferRequestDetailServiceImpl transferRequestDetailServiceImpl;

    @GetMapping("/add")
    public String showCreateForm(
            @RequestParam(value = "fromDepartmentId", required = false) Integer fromDepartmentId,
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "3") int size,
            @RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds,
            Model model) {

        if (!model.containsAttribute("transferRequest")) {
            model.addAttribute("transferRequest", new TransferRequestCreate());
        }

        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("users", userService.findAll());

        if (fromDepartmentId != null) {
            AssetSearchCriteria criteria = new AssetSearchCriteria();
            criteria.setDepartmentId(fromDepartmentId);

            PageResponse<AssetDetailResponse> res =
                    assetService.searchAssets(criteria, pageNo, size);

            model.addAttribute("assets", res.getData());
            model.addAttribute("selectedDepartmentId", fromDepartmentId);
            model.addAttribute("currentPage", res.getCurrentPage());
            model.addAttribute("totalPages", res.getTotalPages());
            model.addAttribute("pageSize", res.getPageSize());
            model.addAttribute("totalRecords", res.getTotalRecords());
            model.addAttribute("selectedIds", selectedIds);
        }

        return "transfer/create";
    }

    @PostMapping("/create")
    public String processCreateForm(@ModelAttribute("transferRequest") TransferRequestCreate request,
                                    RedirectAttributes redirectAttributes) {
        System.out.println("========== START processCreateForm ==========");
        System.out.println("Request received:");
        System.out.println("  - fromDepartmentId: " + request.getFromDepartmentId());
        System.out.println("  - toDepartmentId: " + request.getToDepartmentId());
        System.out.println("  - assetManagerId: " + request.getAssetManagerId());
        System.out.println("  - reason: " + request.getReason());
        System.out.println("  - assetIds: " + request.getAssetIds());

        try {
            TransferResponse response = transferRequestService.createTransferRequest(request);
            System.out.println("Service call successful. Transfer ID: " + response.getTransferId());

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Tạo yêu cầu điều chuyển thành công. Mã yêu cầu: " + response.getTransferId()
            );

            System.out.println("========== END processCreateForm SUCCESS ==========");
            return "redirect:/transfer-requests/list";

        } catch (IllegalArgumentException e) {
            System.out.println("========== IllegalArgumentException ==========");
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("transferRequest", request);

            Integer fromDepartmentId = request.getFromDepartmentId();
            System.out.println("fromDepartmentId from request: " + fromDepartmentId);

            if (fromDepartmentId == null) {
                System.out.println("fromDepartmentId is null, redirecting to add without param");
                return "redirect:/transfer-requests/add";
            }

            String redirectUrl = "/transfer-requests/add?fromDepartmentId=" + fromDepartmentId;
            if (request.getAssetIds() != null && !request.getAssetIds().isEmpty()) {
                for (Integer id : request.getAssetIds()) {
                    redirectUrl += "&selectedIds=" + id;
                }
            }
            System.out.println("Redirect URL: " + redirectUrl);
            System.out.println("========== END processCreateForm ERROR ==========");
            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            System.out.println("========== General Exception ==========");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("transferRequest", request);

            Integer fromDepartmentId = request.getFromDepartmentId();
            if (fromDepartmentId != null) {
                return "redirect:/transfer-requests/add?fromDepartmentId=" + fromDepartmentId;
            }
            return "redirect:/transfer-requests/add";
        }
    }

//    @GetMapping("/{transferId}")
//    public String viewTransferDetail(@PathVariable("transferId") int transferId, Model model) {
//        List<TransferResponse transferResponse = transferRequestDetailServiceImpl.getDetailsByTransferId(transferId);
//        model.addAttribute("transferResponse", transferResponse);
//        return "transfer/detail";
//    }
}



