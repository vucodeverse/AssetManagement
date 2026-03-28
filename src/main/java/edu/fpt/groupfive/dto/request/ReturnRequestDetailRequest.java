package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ReturnRequestDetailRequest {
    private Integer assetId;
    @Pattern(regexp = "^[\\p{L}a-zA-Z0-9 .,_-]*$", message = "Ghi chú không được chứa kí tự đặc biệt")
    private String note;
}
