package com.maning.expensecontrol.controller;

import com.maning.expensecontrol.common.Result;
import com.maning.expensecontrol.entity.SysDepartment;
import com.maning.expensecontrol.mapper.SysDepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final SysDepartmentMapper departmentMapper;// 构造器注入

    @GetMapping
    public Result<List<SysDepartment>> list() {
        return Result.ok(departmentMapper.selectList(null));
    }
}
