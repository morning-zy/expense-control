package com.maning.expensecontrol.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maning.expensecontrol.entity.SysEmployee;

public interface SysEmployeeService {
    /** 查员工详情，查不到返回 null */
    SysEmployee getDetail(Long id);

    /** 按部门分页查员工，deptId 为 null 表示不限制部门 */
    Page<SysEmployee> pageByDept(Long deptId, long page, long size);
}
