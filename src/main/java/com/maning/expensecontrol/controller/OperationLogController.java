package com.maning.expensecontrol.controller;

import com.maning.expensecontrol.common.Result;
import com.maning.expensecontrol.entity.OperationLog;
import com.maning.expensecontrol.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审计轨迹查询。
 * 只提供查询，不提供删除 —— 能改能删的审计日志没有审计意义。
 */
@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private static final int MAX_LIMIT = 200;

    private final OperationLogMapper operationLogMapper;

    /** 查某条业务单据的全部操作历史（按时间正序，像一条时间线） */
    @GetMapping("/{bizType}/{bizId}")
    public Result<List<OperationLog>> list(@PathVariable String bizType,
                                           @PathVariable Long bizId,
                                           @RequestParam(defaultValue = "50") Integer limit) {
        int size = Math.min(limit == null || limit <= 0 ? 50 : limit, MAX_LIMIT);
        List<OperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<OperationLog>()
                        .eq(OperationLog::getBizType, bizType)
                        .eq(OperationLog::getBizId, bizId)
                        .orderByAsc(OperationLog::getId)
                        .last("LIMIT " + size));
        return Result.ok(logs);
    }
}
