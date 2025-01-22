package com.java.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDto {
    private Integer user_role;
    private String user_name;
    private String user_account;
    private String user_password;
    private String user_email;
    private String user_email_code;
}
