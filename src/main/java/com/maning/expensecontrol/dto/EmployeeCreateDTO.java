package com.maning.expensecontrol.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeCreateDTO {

    @NotBlank(message = "员工编号不能为空")
    @Pattern(regexp = "^E\\d{3}$", message = "工号格式必须是 E + 三位数字")
    private String empNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过 64")
    private String name;

    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    @Size(max = 64, message = "职位长度不能超过 64")
    private String position;

    @NotNull(message = "职级不能为空")
    @Min(value = 1, message = "职级最小是 1")
    @Max(value = 5, message = "职级最大是 5")
    private Integer jobLevel;

    private Long managerId;
}
