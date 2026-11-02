package com.maning.expensecontrol.service;

import com.maning.expensecontrol.dto.LoginDTO;
import com.maning.expensecontrol.dto.LoginVO;

public interface AuthService {

    /** 工号 + 密码登录，返回 JWT 令牌与用户基本信息 */
    LoginVO login(LoginDTO dto);

    /** 按用户 id 查基本信息（token 已由拦截器校验） */
    LoginVO profile(Long userId);

    /** 生成密码哈希，供初始化数据 / 重置密码使用 */
    String encodePassword(String rawPassword);
}
