package edu.fpt.groupfive.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UseCreateRequest {
    private String username;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Integer departmentId;
}
