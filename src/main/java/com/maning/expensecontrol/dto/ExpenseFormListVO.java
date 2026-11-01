package com.maning.expensecontrol.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseFormListVO {
    private Long id;
    private String formNo;
    private Long applicantId;
    private String applicantName;      // ← JOIN sys_employee
    private Long deptId;
    private String deptName;           // ← JOIN sys_department
    private Long categoryId;
    private String categoryName;       // ← JOIN expense_category
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String reason;
    private String status;
    private Integer totalNode;
    private LocalDateTime submitTime;
    private LocalDateTime createdAt;
}
