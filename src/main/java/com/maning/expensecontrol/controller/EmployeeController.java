package com.maning.expensecontrol.controller;

import com.maning.expensecontrol.common.PageResult;
import com.maning.expensecontrol.common.Result;
import com.maning.expensecontrol.entity.SysEmployee;
import com.maning.expensecontrol.service.SysEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final SysEmployeeService employeeService;

    @GetMapping("/{id}")
    public Result<SysEmployee> detail(@PathVariable Long id) {
        SysEmployee employee = employeeService.getDetail(id);
        if (employee == null) {
            return Result.fail("员工不存在：" + id);
        }
        return Result.ok(employee);
    }

    @GetMapping
    public Result<PageResult<SysEmployee>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "5") long size,
            @RequestParam(required = false) Long deptId) {

        return Result.ok(new PageResult<>(employeeService.pageByDept(deptId, page, size)));
    }
}
