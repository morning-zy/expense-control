package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("expense_category")
public class ExpenseCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String categoryCode;
    private String categoryName;
    private Integer requireInvoice;
    private BigDecimal singleLimit;   // ← DECIMAL(12,2)，可 NULL
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
