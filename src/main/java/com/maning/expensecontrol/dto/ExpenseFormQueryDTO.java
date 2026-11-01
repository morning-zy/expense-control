package com.maning.expensecontrol.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExpenseFormQueryDTO {
    @Min(value = 1, message = "页码从 1 开始")
    private Long page = 1L;

    @Min(value = 1, message = "每页至少 1 条")
    @Max(value = 100, message = "每页最多 100 条")
    private Long size = 10L;

    /** 状态过滤，null 表示全部 */
    private String status;

    private Long categoryId;

    private Long applicantId;

    /** 关键字：命中单号或事由 */
    private String keyword;

    private LocalDate dateFrom;

    private LocalDate dateTo;
}
