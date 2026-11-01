package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("expense_form")
public class ExpenseForm {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String formNo;
    private Long applicantId;
    private Long deptId;
    private Long categoryId;
    private BigDecimal totalAmount;   // ← BigDecimal，不是 Double
    private Integer itemCount;
    private String reason;
    private String status;
    private Integer currentNode;
    private Integer totalNode;
    private LocalDateTime submitTime;
    private LocalDateTime finishTime;
    @Version
    private Integer version;          // ← 3-1 先加上注解，第 4 阶段生效
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
