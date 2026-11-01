package com.maning.expensecontrol.controller;

import com.maning.expensecontrol.common.PageResult;
import com.maning.expensecontrol.common.Result;
import com.maning.expensecontrol.dto.ExpenseFormCreateDTO;
import com.maning.expensecontrol.dto.ExpenseFormDetailVO;
import com.maning.expensecontrol.dto.ExpenseFormListVO;
import com.maning.expensecontrol.dto.ExpenseFormQueryDTO;
import com.maning.expensecontrol.service.ExpenseFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expense-forms")
@RequiredArgsConstructor
public class ExpenseFormController {

    private final ExpenseFormService expenseFormService;

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ExpenseFormCreateDTO dto) {
        return Result.ok(expenseFormService.create(dto));
    }

    @GetMapping
    public Result<PageResult<ExpenseFormListVO>> list(@Valid ExpenseFormQueryDTO query) {
        return Result.ok(expenseFormService.pageForms(query));
    }

    @GetMapping("/{id}")
    public Result<ExpenseFormDetailVO> detail(@PathVariable Long id) {
        return Result.ok(expenseFormService.getDetail(id));
    }
}
