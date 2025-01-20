package com.java.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//示例
//        {
//        "user_id": "386807e7-44f3-4dac-8eef-499f0084249e",
//        "user_name": "天万佳",
//        "user_account": "zhangsan1225",
//        "user_password": "xxxxx",
//        "user_email": "vacsbt_fim45@example.net",
//        "user_email_code": "xxxxx-xxxxx-xxxxx-xxxxx"
//        }
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    private String user_id;
    private String user_name;
    private String user_account;
    private String user_password;
    private String user_email;
    private String user_email_code;
}
