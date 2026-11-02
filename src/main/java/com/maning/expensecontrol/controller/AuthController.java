package com.maning.expensecontrol.controller;

import com.maning.expensecontrol.common.Result;
import com.maning.expensecontrol.common.UserContext;
import com.maning.expensecontrol.dto.LoginDTO;
import com.maning.expensecontrol.dto.LoginVO;
import com.maning.expensecontrol.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录：工号 + 密码，返回 JWT */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    /** 当前登录人信息（前端刷新页面后用它恢复用户态） */
    @GetMapping("/me")
    public Result<LoginVO> me() {
        return Result.ok(authService.profile(UserContext.requireUserId()));
    }
}
