package edu.fpt.groupfive.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UseCreateRequest {
    private String username;

    private String password;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String Role;

    private Integer departmentId;

}
