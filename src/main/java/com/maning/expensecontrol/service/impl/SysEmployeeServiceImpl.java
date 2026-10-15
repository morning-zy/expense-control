package com.maning.expensecontrol.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maning.expensecontrol.entity.SysEmployee;
import com.maning.expensecontrol.mapper.SysEmployeeMapper;
import com.maning.expensecontrol.service.SysEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysEmployeeServiceImpl implements SysEmployeeService {

    private final SysEmployeeMapper employeeMapper;

    @Override
    public SysEmployee getDetail(Long id) {
        //用Mapper按主键查
        return employeeMapper.selectById(id);
    }

    @Override
    public Page<SysEmployee> pageByDept(Long deptId, long page, long size) {
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<SysEmployee>()
                .eq(deptId!=null,SysEmployee::getDeptId,deptId)
                .orderByAsc(SysEmployee::getId);
        return employeeMapper.selectPage(new Page<>(page,size),wrapper);
    }
}
