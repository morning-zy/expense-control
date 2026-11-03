package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 操作留痕（只增不改不删，审计轨迹）。
 * 注意这张表没有 deleted 字段 —— 审计记录一旦能删就不叫审计了。
 */
@Getter
@Setter
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizType;

    private Long bizId;

    private Long operatorId;

    private String action;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private String ip;

    private LocalDateTime createdAt;
}
