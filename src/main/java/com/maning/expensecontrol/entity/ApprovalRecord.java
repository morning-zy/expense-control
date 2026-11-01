package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 审批流水（只增不改不删，审计凭证）。
 * 与 approval_task 的区别：task 是「待办」，record 是「已办」。
 */
@Getter
@Setter
@TableName("approval_record")
public class ApprovalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long formId;

    private Integer nodeSeq;

    private String nodeName;

    private Long approverId;

    /** SUBMIT / APPROVE / REJECT */
    private String action;

    private String approvalComment;

    private LocalDateTime createdAt;
}
