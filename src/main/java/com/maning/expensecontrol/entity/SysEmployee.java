package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sys_employee")
public class SysEmployee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String empNo;
    private String name;
    private Long deptId;
    private String position;
    private Integer jobLevel;
    private Long managerId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
