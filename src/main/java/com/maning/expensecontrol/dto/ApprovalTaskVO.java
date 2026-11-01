package com.maning.expensecontrol.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 审批节点视图（审批进度 / 我的待办）。
 */
@Getter
@Setter
public class ApprovalTaskVO {

    private Long id;

    private Long formId;

    private String formNo;

    private String applicantName;

    private Integer nodeSeq;

    private String nodeName;

    private Long approverId;

    private String approverName;

    private String status;

    private String approvalComment;

    private LocalDateTime decidedAt;
}
