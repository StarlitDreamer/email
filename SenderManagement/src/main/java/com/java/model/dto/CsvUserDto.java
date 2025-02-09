package com.java.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.opencsv.bean.CsvBindByName;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CsvUserDto {

  @CsvBindByName
  private String userRole;//角色管理
  @CsvBindByName
  private String userName;//用户名
  @CsvBindByName
  private String userAccount;//用户账号
  @CsvBindByName
  private String userPassword;//用户密码
  @CsvBindByName
  private String userEmail;//用户邮箱
  @CsvBindByName
  private String userEmailCode;//用户邮箱授权码
}
