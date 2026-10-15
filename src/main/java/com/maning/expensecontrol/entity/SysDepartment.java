package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sys_department")
public class SysDepartment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String deptCode;
    private String deptName;
    private Long parentId;
    private Long managerId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
