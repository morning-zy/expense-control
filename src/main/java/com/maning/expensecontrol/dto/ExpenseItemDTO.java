package com.maning.expensecontrol.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpenseItemDTO {
    @NotNull(message = "费用发生日期不能为空")
    private LocalDate expenseDate;

    @NotNull(message = "明细金额不能为空")
    @DecimalMin(value = "0.01", message = "明细金额必须大于 0")
    @Digits(integer = 10, fraction = 2, message = "金额最多 10 位整数、2 位小数")
    private BigDecimal amount;

    @Size(max = 255, message = "费用说明不能超过 255 字")
    private String description;

    @Size(max = 64, message = "发票号码不能超过 64 位")
    private String invoiceNo;

    @Size(max = 32, message = "发票代码不能超过 32 位")
    private String invoiceCode;
}
