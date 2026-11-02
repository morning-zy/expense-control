package com.maning.expensecontrol.controller;

import com.maning.expensecontrol.common.Result;
import com.maning.expensecontrol.common.UserContext;
import com.maning.expensecontrol.dto.ApprovalDecisionDTO;
import com.maning.expensecontrol.dto.ApprovalTaskVO;
import com.maning.expensecontrol.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批接口。
 * <p>
 * 当前登录人由 AuthInterceptor 从 JWT 解出并放进 UserContext，
 * 客户端不能再通过请求头冒充别人 —— 这是接登录前后最大的区别。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    /** 提交审批 */
    @PostMapping("/expense-forms/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        approvalService.submit(id);
        return Result.ok();
    }

    /** 审批通过 */
    @PostMapping("/expense-forms/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                                @Valid @RequestBody(required = false) ApprovalDecisionDTO dto) {
        approvalService.approve(id, UserContext.requireUserId(), dto == null ? null : dto.getComment());
        return Result.ok();
    }

    /** 驳回 */
    @PostMapping("/expense-forms/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @Valid @RequestBody(required = false) ApprovalDecisionDTO dto) {
        approvalService.reject(id, UserContext.requireUserId(), dto == null ? null : dto.getComment());
        return Result.ok();
    }

    /** 某张单的审批进度 */
    @GetMapping("/expense-forms/{id}/tasks")
    public Result<List<ApprovalTaskVO>> tasks(@PathVariable Long id) {
        return Result.ok(approvalService.listTasks(id));
    }

    /** 我的待办 */
    @GetMapping("/approvals/pending")
    public Result<List<ApprovalTaskVO>> pending() {
        return Result.ok(approvalService.myPending(UserContext.requireUserId()));
    }
}
