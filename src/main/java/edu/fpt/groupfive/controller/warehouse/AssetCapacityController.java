package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.AssetCapacityRequest;
import edu.fpt.groupfive.service.warehouse.AssetCapacityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse/capacities")
public class AssetCapacityController {

    private final AssetCapacityService capacityService;

    @GetMapping("/asset-type/{assetTypeId}")
    public String getCapacityByAssetTypeId(@PathVariable("assetTypeId") Integer assetTypeId, Model model) {
        model.addAttribute("capacity", capacityService.getCapacityByAssetTypeId(assetTypeId));
        return "warehouse/capacity-detail";
    }

    @PostMapping("/create")
    public String createOrUpdateCapacity(@ModelAttribute AssetCapacityRequest request) {
        capacityService.createOrUpdateCapacity(request);
        return "redirect:/warehouse/capacities/asset-type/" + request.getAssetTypeId();
    }
}
