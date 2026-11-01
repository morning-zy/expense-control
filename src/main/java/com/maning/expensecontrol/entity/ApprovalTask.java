package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 审批任务：报销单提交时，按审批链「快照」出来的待办节点。
 * <p>
 * 为什么不只用 approval_record：
 * 1. record 只记「已发生」的动作，查不出「某人今天有几张待办」；
 * 2. 组织架构变了以后，动态重算会把已提交单据的审批路径悄悄改写 —— 快照才能保证历史一致。
 */
@Getter
@Setter
@TableName("approval_task")
public class ApprovalTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long formId;

    /** 节点序号：1/2/3 */
    private Integer nodeSeq;

    /** 节点名称：主管审批 / 部门经理审批 / 总经理审批 */
    private String nodeName;

    /** 该节点审批人（提交时快照，之后组织架构变更不影响） */
    private Long approverId;

    /** PENDING / APPROVED / REJECTED / SKIPPED */
    private String status;

    private String approvalComment;

    private LocalDateTime decidedAt;

    private LocalDateTime createdAt;
}
