package edu.fpt.groupfive.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Users {

    private Integer userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private Integer phone;
    private String email;
    private Date createdAt;
    private String status;
    private String role;
    private Date updatedAt;
}
