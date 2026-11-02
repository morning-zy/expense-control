package com.maning.expensecontrol.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maning.expensecontrol.dto.LoginDTO;
import com.maning.expensecontrol.dto.LoginVO;
import com.maning.expensecontrol.entity.SysDepartment;
import com.maning.expensecontrol.entity.SysEmployee;
import com.maning.expensecontrol.exception.BusinessException;
import com.maning.expensecontrol.exception.ErrorCode;
import com.maning.expensecontrol.mapper.SysDepartmentMapper;
import com.maning.expensecontrol.mapper.SysEmployeeMapper;
import com.maning.expensecontrol.security.JwtUtil;
import com.maning.expensecontrol.security.PasswordHasher;
import com.maning.expensecontrol.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysEmployeeMapper employeeMapper;
    private final SysDepartmentMapper departmentMapper;
    private final JwtUtil jwtUtil;
    private final PasswordHasher passwordHasher;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysEmployee emp = employeeMapper.selectOne(new LambdaQueryWrapper<SysEmployee>()
                .eq(SysEmployee::getEmpNo, dto.getEmpNo()));

        // 「工号不存在」和「密码错误」返回同一个错误，避免被用来枚举工号
        if (emp == null || emp.getStatus() == null || emp.getStatus() != 1
                || !passwordHasher.matches(dto.getPassword(), emp.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        LoginVO vo = toVO(emp);
        vo.setToken(jwtUtil.generate(emp.getId(), emp.getEmpNo()));
        vo.setExpiresIn(jwtUtil.getExpireSeconds());
        return vo;
    }

    @Override
    public LoginVO profile(Long userId) {
        SysEmployee emp = employeeMapper.selectById(userId);
        if (emp == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号不存在，请重新登录");
        }
        return toVO(emp);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordHasher.hash(rawPassword);
    }

    private LoginVO toVO(SysEmployee emp) {
        LoginVO vo = new LoginVO();
        vo.setUserId(emp.getId());
        vo.setEmpNo(emp.getEmpNo());
        vo.setName(emp.getName());
        vo.setPosition(emp.getPosition());
        vo.setJobLevel(emp.getJobLevel());
        vo.setDeptId(emp.getDeptId());
        SysDepartment dept = departmentMapper.selectById(emp.getDeptId());
        vo.setDeptName(dept == null ? null : dept.getDeptName());
        return vo;
    }
}
