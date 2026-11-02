package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /** PBKDF2 加盐哈希，格式 pbkdf2$迭代次数$盐$摘要；永远不返回给前端 */
    @JsonIgnore
    private String passwordHash;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
