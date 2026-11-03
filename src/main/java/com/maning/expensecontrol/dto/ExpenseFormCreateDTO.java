package com.maning.expensecontrol.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString                    // 操作留痕切面会把入参 toString 写进审计表，没有它只会打印对象地址
public class ExpenseFormCreateDTO {
    @NotNull(message = "费用科目不能为空")
    private Long categoryId;

    @Size(max = 500, message = "报销事由不能超过 500 字")
    private String reason;

    @NotEmpty(message = "报销明细不能为空")
    @Valid                                    // ← 关键！不加这个，items 里每个元素的校验不生效
    @Size(max = 50, message = "单张报销单最多 50 条明细")
    private List<ExpenseItemDTO> items;
}
