package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("expense_item")
public class ExpenseItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long formId;
    private LocalDate expenseDate;    // ← DATE 对应 LocalDate，不是 LocalDateTime
    private BigDecimal amount;
    private String description;
    private String invoiceNo;
    private String invoiceCode;
    private LocalDateTime createdAt;
}
