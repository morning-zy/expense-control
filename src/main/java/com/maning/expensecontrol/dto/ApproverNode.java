package com.maning.expensecontrol.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 审批链上的一个节点（递归 CTE 查出来的原始结果，不是快照）。
 */
@Getter
@Setter
public class ApproverNode {

    private Long approverId;

    private String approverName;

    private Integer jobLevel;

    /** 从申请人的上级开始，1 表示最靠近申请人的那一级 */
    private Integer nodeSeq;
}
