package com.maning.expensecontrol.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maning.expensecontrol.dto.EmployeeCreateDTO;
import com.maning.expensecontrol.entity.SysEmployee;
import com.maning.expensecontrol.exception.BusinessException;
import com.maning.expensecontrol.exception.ErrorCode;
import com.maning.expensecontrol.mapper.SysDepartmentMapper;
import com.maning.expensecontrol.mapper.SysEmployeeMapper;
import com.maning.expensecontrol.service.SysEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysEmployeeServiceImpl implements SysEmployeeService {

    private final SysEmployeeMapper employeeMapper;
    private final SysDepartmentMapper departmentMapper;

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

    @Override
    public Long create(EmployeeCreateDTO dto) {
        // 1,工号唯一，先count再插入
        Long duplicated=employeeMapper.selectCount(
                new LambdaQueryWrapper<SysEmployee>().eq(SysEmployee::getEmpNo,dto.getEmpNo()));
        if(duplicated>0){
            throw new BusinessException(ErrorCode.EMP_NO_DUPLICATE,"工号已存在:"+dto.getEmpNo());

        }
        // 2.部门必须存在，否则会插入一条悬空数据
        if (departmentMapper.selectById(dto.getDeptId())==null){
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND,"所属部门不存在:"+dto.getDeptId());
        }
        SysEmployee employee = new SysEmployee();
        employee.setEmpNo(dto.getEmpNo());
        employee.setName(dto.getName());
        employee.setDeptId(dto.getDeptId());
        employee.setPosition(dto.getPosition());
        employee.setJobLevel(dto.getJobLevel());
        employee.setManagerId(dto.getManagerId());
        employee.setStatus(1);                 // 3. 新增默认在职，不由前端传
        employeeMapper.insert(employee);

        return employee.getId();               // 4. 插入后 id 被回填
    }
}
