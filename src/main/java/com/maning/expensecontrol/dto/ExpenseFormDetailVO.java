package com.maning.expensecontrol.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ExpenseFormDetailVO {
    private Long id;
    private String formNo;
    private Long applicantId;
    private String applicantName;
    private Long deptId;
    private String deptName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String reason;
    private String status;
    private Integer currentNode;
    private Integer totalNode;
    private List<ItemVO> items;        // ← 明细数组

    @Getter
    @Setter
    public static class ItemVO {
        private Long id;
        private LocalDate expenseDate;
        private BigDecimal amount;
        private String description;
        private String invoiceNo;
        private String invoiceCode;
    }
}
