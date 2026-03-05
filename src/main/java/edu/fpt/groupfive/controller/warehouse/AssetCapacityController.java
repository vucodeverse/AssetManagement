package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.AssetCapacityRequest;
import edu.fpt.groupfive.service.warehouse.AssetCapacityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse/capacities")
public class AssetCapacityController {

    @Autowired
    private AssetCapacityService capacityService;

    @GetMapping("/asset-type/{assetTypeId}")
    public String getCapacityByAssetTypeId(@PathVariable Integer assetTypeId, Model model) {
        model.addAttribute("capacity", capacityService.getCapacityByAssetTypeId(assetTypeId));
        return "warehouse/capacity-detail";
    }

    @PostMapping("/create")
    public String createOrUpdateCapacity(@ModelAttribute AssetCapacityRequest request) {
        capacityService.createOrUpdateCapacity(request);
        return "redirect:/warehouse/capacities/asset-type/" + request.getAssetTypeId();
    }
}
