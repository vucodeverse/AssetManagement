package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.RackReqDto;
import edu.fpt.groupfive.dto.warehouse.RackRespDto;
import edu.fpt.groupfive.dto.warehouse.WarehouseRespDto;
import edu.fpt.groupfive.model.warehouse.Rack;
import edu.fpt.groupfive.service.warehouse.RackService;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/wh/warehouses/{whId}/racks")
public class RackController {

    private final RackService rackService;
    private final WarehouseService warehouseService;

    private void loadWarehouse(Integer whId, Model model) {
        WarehouseRespDto warehouse = warehouseService.getWarehouseDetail(whId);
        model.addAttribute("warehouse", warehouse);
    }

    @GetMapping
    public String listRacks(@PathVariable("whId") Integer whId, Model model) {
        loadWarehouse(whId, model);
        model.addAttribute("racks", rackService.getAllRacksByWarehouse(whId));
        return "page/warehouse/rack/list-view";
    }

    @GetMapping("/add")
    public String showAddForm(@PathVariable("whId") Integer whId, Model model) {
        loadWarehouse(whId, model);
        model.addAttribute("rack", RackReqDto.builder().build());
        return "page/warehouse/rack/add-form";
    }

    @PostMapping("/add")
    public String addRack(
            @PathVariable("whId") Integer whId,
            @Valid @ModelAttribute("rack") RackReqDto request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            loadWarehouse(whId, model);
            return "page/warehouse/rack/add-form";
        }

        Rack created = rackService.createRack(whId, request);
        return "redirect:/wh/warehouses/" + whId + "/racks";
    }

    @GetMapping("/{rackId}/edit")
    public String showEditForm(@PathVariable("whId") Integer whId,
                               @PathVariable("rackId") Integer rackId,
                               Model model) {
        loadWarehouse(whId, model);
        RackRespDto rack = rackService.getRackDetail(rackId);
        model.addAttribute("rack", new RackReqDto(rack.name(), rack.description()));
        model.addAttribute("rackId", rackId);
        return "page/warehouse/rack/edit-form";
    }

    @PostMapping("/{rackId}/edit")
    public String editRack(
            @PathVariable("whId") Integer whId,
            @PathVariable("rackId") Integer rackId,
            @Valid @ModelAttribute("rack") RackReqDto request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            loadWarehouse(whId, model);
            model.addAttribute("rackId", rackId);
            return "page/warehouse/rack/edit-form";
        }

        rackService.updateRack(rackId, request);
        return "redirect:/wh/warehouses/" + whId + "/racks";
    }

    @PostMapping("/{rackId}/delete")
    public String deleteRack(@PathVariable("whId") Integer whId,
                             @PathVariable("rackId") Integer rackId) {
        rackService.deleteRack(rackId);
        return "redirect:/wh/warehouses/" + whId + "/racks";
    }
}
