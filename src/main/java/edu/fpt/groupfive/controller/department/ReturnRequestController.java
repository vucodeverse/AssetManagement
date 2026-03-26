package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.dto.response.ReturnRequestRespnse;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.ReturnRequestService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/department/return-request")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;
    private final AssetService assetService;

    @GetMapping("/list")
    public String showList(HttpSession session, Model model) {

        Integer departmentId = (Integer) session.getAttribute("departmentId");

        List<ReturnRequestRespnse> list = returnRequestService.getAllRequest(departmentId);

        model.addAttribute("requests", list);

        return "return/return_request_list";
    }

    @GetMapping("/search")
    public String searchAction(
            HttpSession session,
            @RequestParam(required = false, name = "keyword") String keyword,
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false, name = "fromDate") String fromDate,
            @RequestParam(required = false, name = "toDate") String toDate,
            Model model) {

        LocalDate from = null;

        LocalDate to = null;

        if (fromDate != null && !fromDate.isEmpty()) {
            from = LocalDate.parse(fromDate);
        }

        if (toDate != null && !toDate.isEmpty()) {
            to = LocalDate.parse(toDate);
        }

        Integer departmentId = (Integer) session.getAttribute("departmentId");

        List<ReturnRequestRespnse> list = returnRequestService
                .searchRequest(departmentId, keyword, status, from, to);

        model.addAttribute("requests", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "return/return_request_list";
    }

    @GetMapping("/detail/{id}")
    public String showDetailForm(
            @PathVariable("id") Integer id,
            Model model) {
        //Lấy request cần tìm
        ReturnRequestRespnse respnse = returnRequestService.getRequestById(id);

        model.addAttribute("requestDto", respnse);

        model.addAttribute("assets", assetService.getAllByReturnRequestId(id));

        model.addAttribute("canEdit", false);

        return "return/return_request_detail";

    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {

        Integer departmentId = (Integer) session.getAttribute("departmentId");

        model.addAttribute("requestDto", new ReturnRequestCreateRequest());

        model.addAttribute("assets", assetService.getAllByDepartmentId(departmentId));

        model.addAttribute("canEdit", true);

        return "return/return_request_form";
    }

    @PostMapping("/save")
    public String saveRequest(
            @Valid @ModelAttribute("requestDto") ReturnRequestCreateRequest requestDto,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer departmentId = (Integer) session.getAttribute("departmentId");

        if (bindingResult.hasErrors()) {

            model.addAttribute("requestDto", requestDto);
            model.addAttribute("assets", assetService.getAllByDepartmentId(departmentId));
            model.addAttribute("canEdit", true);

            return "return/return_request_form";
        }

        requestDto.setRequesterId((Integer) session.getAttribute("userId"));
        requestDto.setRequestedDepartmentId(departmentId);
        returnRequestService.createRequest(requestDto);


        redirectAttributes.addFlashAttribute("message", "Gửi yêu cầu thành công!");

        return "redirect:/department/return-request/list";


    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable("id") Integer id,
            HttpSession session,
            Model model) {
        // Lấy request cần update
        ReturnRequestRespnse dto = returnRequestService.getRequestById(id);

        Integer departmentId = (Integer) session.getAttribute("departmentId");

        model.addAttribute("requestDto", dto);

        model.addAttribute("assetNew", assetService.getAllByDepartmentId(departmentId));

        model.addAttribute("assets", assetService.getAllByReturnRequestId(id));

        model.addAttribute("canEdit", true);

        return "return/return_request_form";

    }

    @PostMapping("/update/{id}")
    public String updateRequest(
            @PathVariable("id") Integer id,
            @ModelAttribute("requestDto") ReturnRequestCreateRequest dto,
            RedirectAttributes redirectAttributes) {

        try {

            returnRequestService.updateRequest(id, dto);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Cập nhật thành công!"
            );

            return "redirect:/department/return-request/list";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/department/return-request/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteRequest(
            @PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {

        try {
            returnRequestService.deleteRequest(id);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Xóa thành công!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/department/return-request/list";
    }


}
