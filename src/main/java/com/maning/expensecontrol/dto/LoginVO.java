package com.maning.expensecontrol.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginVO {

    private String token;

    private String tokenType = "Bearer";

    private Long expiresIn;

    private Long userId;

    private String empNo;

    private String name;

    private String position;

    private Integer jobLevel;

    private Long deptId;

    private String deptName;
}
