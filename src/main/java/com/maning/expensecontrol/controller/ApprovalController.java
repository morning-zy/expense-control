package com.maning.expensecontrol.controller;

import com.maning.expensecontrol.common.Result;
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
 * 当前登录用户暂时用请求头 X-User-Id 传（默认 1 = 张伟）。
 * 第 7 阶段接入 JWT 时，把这一行换成「从 token 里取」即可，Service 不用改。
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
                                @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
                                @Valid @RequestBody(required = false) ApprovalDecisionDTO dto) {
        approvalService.approve(id, userId, dto == null ? null : dto.getComment());
        return Result.ok();
    }

    /** 驳回 */
    @PostMapping("/expense-forms/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
                               @Valid @RequestBody(required = false) ApprovalDecisionDTO dto) {
        approvalService.reject(id, userId, dto == null ? null : dto.getComment());
        return Result.ok();
    }

    /** 某张单的审批进度 */
    @GetMapping("/expense-forms/{id}/tasks")
    public Result<List<ApprovalTaskVO>> tasks(@PathVariable Long id) {
        return Result.ok(approvalService.listTasks(id));
    }

    /** 我的待办 */
    @GetMapping("/approvals/pending")
    public Result<List<ApprovalTaskVO>> pending(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return Result.ok(approvalService.myPending(userId));
    }
}
