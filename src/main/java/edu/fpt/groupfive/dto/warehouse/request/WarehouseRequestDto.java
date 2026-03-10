package edu.fpt.groupfive.dto.warehouse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WarehouseRequestDto {

        @NotBlank(message = "Tên kho không được để trống")
        @Size(max = 100, message = "Tên kho không được vượt quá 100 ký tự")
        private String name;

        @NotBlank(message = "Địa chỉ kho không được để trống")
        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
        private String address;

        @NotNull(message = "Người quản lý kho không được để trống")
        private Integer managerUserId;
}
