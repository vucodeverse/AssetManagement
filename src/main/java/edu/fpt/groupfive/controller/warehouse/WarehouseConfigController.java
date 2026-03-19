package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.AssetVolumeUpdateRequestDTO;
import edu.fpt.groupfive.dto.request.warehouse.ZoneCreateRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.AssetTypeVolumeDTO;
import edu.fpt.groupfive.dto.response.warehouse.ZoneCapacityResponseDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.RequiredArgsConstructor;
import edu.fpt.groupfive.service.WhZoneService;

import java.util.List;

/**
 * Controller quản lý không gian kho: Zone & Định mức sức chứa loại tài sản.
 * Dùng dummy data để review luồng UI trước khi nối DAO/Service thật.
 */
@Controller
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseConfigController {

    private final WhZoneService whZoneService;

    private static final String REDIRECT_ZONES       = "redirect:/warehouse/zones";
    private static final String REDIRECT_CAPACITIES  = "redirect:/warehouse/capacities";
    private static final String SUCCESS_MSG          = "successMessage";
    private static final String ERROR_MSG            = "errorMessage";
    private static final String ZONE_FORM            = "zoneForm";
    private static final String VOLUME_FORM          = "volumeForm";

    // =========================================================
    //  ZONE LIST  —  GET /warehouse/zones
    // =========================================================

    @GetMapping("/zones")
    public String zonesPage(Model model) {
        model.addAttribute("activeMenu", "zones");
        model.addAttribute("pageTitle", "Quản lý Zone - Warehouse");

        List<ZoneCapacityResponseDTO> zones = whZoneService.getAllZones();
        model.addAttribute("zones", zones);

        long countEmpty = zones.stream().filter(z -> "EMPTY".equals(z.getStatusFlag())).count();
        long countInUse = zones.stream().filter(z -> "IN_USE".equals(z.getStatusFlag())).count();
        long countFull  = zones.stream().filter(z -> "FULL".equals(z.getStatusFlag())).count();
        model.addAttribute("countEmpty", countEmpty);
        model.addAttribute("countInUse", countInUse);
        model.addAttribute("countFull",  countFull);

        if (!model.containsAttribute(ZONE_FORM)) {
            model.addAttribute(ZONE_FORM, new ZoneCreateRequestDTO());
        }
        return "warehouse/zones";
    }

    /** POST: Tạo zone mới — PRG pattern */
    @PostMapping("/zones/create")
    public String createZone(
            @Valid @ModelAttribute(ZONE_FORM) ZoneCreateRequestDTO dto,
            BindingResult result, RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult." + ZONE_FORM, result);
            ra.addFlashAttribute(ZONE_FORM, dto);
            ra.addFlashAttribute(ERROR_MSG, "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
            return REDIRECT_ZONES;
        }
        whZoneService.createZone(dto);
        ra.addFlashAttribute(SUCCESS_MSG, "Tạo zone \"" + dto.getZoneName() + "\" thành công.");
        return REDIRECT_ZONES;
    }

    // =========================================================
    //  ZONE DETAIL  —  GET /warehouse/zones/{zoneId}
    // =========================================================

    @GetMapping("/zones/{zoneId}")
    public String zoneDetail(@PathVariable("zoneId") int zoneId, Model model) {
        model.addAttribute("activeMenu", "zones");
        model.addAttribute("pageTitle", "Chi tiết Zone - Warehouse");

        try {
            ZoneCapacityResponseDTO zone = whZoneService.getZoneById(zoneId);
            model.addAttribute("zone", zone);
            return "warehouse/zone-detail";
        } catch (Exception e) {
            return REDIRECT_ZONES;
        }
    }

    // =========================================================
    //  ZONE EDIT FORM  —  GET /warehouse/zones/{zoneId}/edit
    // =========================================================

    @GetMapping("/zones/{zoneId}/edit")
    public String zoneEditForm(@PathVariable("zoneId") int zoneId, Model model) {
        model.addAttribute("activeMenu", "zones");
        model.addAttribute("pageTitle", "Cập nhật Zone - Warehouse");

        ZoneCapacityResponseDTO zone;
        try {
            zone = whZoneService.getZoneById(zoneId);
        } catch (Exception e) {
            return REDIRECT_ZONES;
        }
        model.addAttribute("zone", zone);

        if (!model.containsAttribute("editForm")) {
            // Điền sẵn giá trị hiện tại vào form
            ZoneCreateRequestDTO editForm = new ZoneCreateRequestDTO();
            editForm.setZoneName(zone.getZoneName());
            editForm.setMaxCapacity(zone.getMaxCapacity());
            model.addAttribute("editForm", editForm);
        }
        return "warehouse/zone-edit";
    }

    // =========================================================
    //  ZONE UPDATE  —  POST /warehouse/zones/{zoneId}/update
    // =========================================================

    @PostMapping("/zones/{zoneId}/update")
    public String updateZone(
            @PathVariable("zoneId") int zoneId,
            @Valid @ModelAttribute("editForm") ZoneCreateRequestDTO dto,
            BindingResult result, RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.editForm", result);
            ra.addFlashAttribute("editForm", dto);
            ra.addFlashAttribute(ERROR_MSG, "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
            return "redirect:/warehouse/zones/" + zoneId + "/edit";
        }

        try {
            whZoneService.updateZone(zoneId, dto);
            ra.addFlashAttribute(SUCCESS_MSG,
                    "Cập nhật zone \"" + dto.getZoneName() + "\" thành công.");
            return "redirect:/warehouse/zones/" + zoneId;
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute(ERROR_MSG, ex.getMessage());
            ra.addFlashAttribute("editForm", dto);
            return "redirect:/warehouse/zones/" + zoneId + "/edit";
        }
    }

    // =========================================================
    //  ASSET CAPACITY CONFIG  —  GET /warehouse/capacities
    // =========================================================

    @GetMapping("/capacities")
    public String capacitiesPage(Model model) {
        model.addAttribute("activeMenu", "capacities");
        model.addAttribute("pageTitle", "Định mức Sức chứa - Warehouse");
        model.addAttribute("assetTypes", buildDummyAssetTypes());

        if (!model.containsAttribute(VOLUME_FORM)) {
            model.addAttribute(VOLUME_FORM, new AssetVolumeUpdateRequestDTO());
        }
        return "warehouse/capacities";
    }

    /** POST: Cập nhật unit_volume cho loại tài sản — PRG pattern */
    @PostMapping("/capacities/update")
    public String updateAssetVolume(
            @Valid @ModelAttribute(VOLUME_FORM) AssetVolumeUpdateRequestDTO dto,
            BindingResult result, RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult." + VOLUME_FORM, result);
            ra.addFlashAttribute(VOLUME_FORM, dto);
            ra.addFlashAttribute(ERROR_MSG, "Định mức không hợp lệ.");
            return REDIRECT_CAPACITIES;
        }
        // TODO: service.updateAssetVolume(dto)
        ra.addFlashAttribute(SUCCESS_MSG,
                "Cập nhật định mức loại #" + dto.getAssetTypeId()
                        + " → " + dto.getUnitVolume() + " đv/tài sản (demo).");
        return REDIRECT_CAPACITIES;
    }

    // =========================================================
    //  DUMMY DATA — xoá khi nối service + DAO thật
    // =========================================================


    private List<AssetTypeVolumeDTO> buildDummyAssetTypes() {
        return List.of(
                AssetTypeVolumeDTO.builder()
                        .assetTypeId(1).typeName("Laptop Dell XPS").unitVolume(1)
                        .categoryName("Máy tính xách tay").build(),
                AssetTypeVolumeDTO.builder()
                        .assetTypeId(2).typeName("Máy in HP LaserJet").unitVolume(3)
                        .categoryName("Thiết bị in ấn").build(),
                AssetTypeVolumeDTO.builder()
                        .assetTypeId(3).typeName("Màn hình LG 24\"").unitVolume(2)
                        .categoryName("Thiết bị hiển thị").build(),
                AssetTypeVolumeDTO.builder()
                        .assetTypeId(4).typeName("Bàn phím Logitech").unitVolume(1)
                        .categoryName("Phụ kiện văn phòng").build(),
                AssetTypeVolumeDTO.builder()
                        .assetTypeId(5).typeName("Máy chiếu Epson").unitVolume(null)
                        .categoryName("Thiết bị trình chiếu").build()
        );
    }
}
