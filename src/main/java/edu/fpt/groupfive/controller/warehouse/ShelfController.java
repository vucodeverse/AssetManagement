package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.RackRespDto;
import edu.fpt.groupfive.dto.warehouse.ShelfReqDto;
import edu.fpt.groupfive.dto.warehouse.ShelfRespDto;
import edu.fpt.groupfive.model.warehouse.Shelf;
import edu.fpt.groupfive.service.warehouse.RackService;
import edu.fpt.groupfive.service.warehouse.ShelfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/wh/warehouses/{whId}/racks/{rackId}/shelves")
public class ShelfController {

    private final ShelfService shelfService;
    private final RackService rackService;

    private void loadRack(Integer rackId, Model model) {
        RackRespDto rack = rackService.getRackDetail(rackId);
        model.addAttribute("rack", rack);
    }

    @GetMapping
    public String listShelves(@PathVariable("whId") Integer whId,
                              @PathVariable("rackId") Integer rackId,
                              Model model) {
        loadRack(rackId, model);
        model.addAttribute("shelves", shelfService.getAllShelvesByRack(rackId));
        model.addAttribute("whId", whId);
        return "page/warehouse/shelf/list-view";
    }

    @GetMapping("/add")
    public String showAddForm(@PathVariable("whId") Integer whId,
                              @PathVariable("rackId") Integer rackId,
                              Model model) {
        loadRack(rackId, model);
        model.addAttribute("shelf", ShelfReqDto.builder().build());
        model.addAttribute("whId", whId);
        return "page/warehouse/shelf/add-form";
    }

    @PostMapping("/add")
    public String addShelf(
            @PathVariable("whId") Integer whId,
            @PathVariable("rackId") Integer rackId,
            @Valid @ModelAttribute("shelf") ShelfReqDto request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            loadRack(rackId, model);
            model.addAttribute("whId", whId);
            return "page/warehouse/shelf/add-form";
        }

        shelfService.createShelf(rackId, request);
        return "redirect:/wh/warehouses/" + whId + "/racks/" + rackId + "/shelves";
    }

    @GetMapping("/{shelfId}/edit")
    public String showEditForm(@PathVariable("whId") Integer whId,
                               @PathVariable("rackId") Integer rackId,
                               @PathVariable("shelfId") Integer shelfId,
                               Model model) {
        loadRack(rackId, model);
        ShelfRespDto shelf = shelfService.getShelfDetail(shelfId);
        model.addAttribute("shelf", new ShelfReqDto(shelf.name(), shelf.maxCapacity(), shelf.description()));
        model.addAttribute("shelfId", shelfId);
        model.addAttribute("whId", whId);
        model.addAttribute("currentCapacity", shelf.currentCapacity());
        return "page/warehouse/shelf/edit-form";
    }

    @PostMapping("/{shelfId}/edit")
    public String editShelf(
            @PathVariable("whId") Integer whId,
            @PathVariable("rackId") Integer rackId,
            @PathVariable("shelfId") Integer shelfId,
            @Valid @ModelAttribute("shelf") ShelfReqDto request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            loadRack(rackId, model);
            model.addAttribute("shelfId", shelfId);
            model.addAttribute("whId", whId);
            return "page/warehouse/shelf/edit-form";
        }

        shelfService.updateShelf(shelfId, request);
        return "redirect:/wh/warehouses/" + whId + "/racks/" + rackId + "/shelves";
    }

    @PostMapping("/{shelfId}/delete")
    public String deleteShelf(@PathVariable("whId") Integer whId,
                              @PathVariable("rackId") Integer rackId,
                              @PathVariable("shelfId") Integer shelfId) {
        shelfService.deleteShelf(shelfId);
        return "redirect:/wh/warehouses/" + whId + "/racks/" + rackId + "/shelves";
    }
}
