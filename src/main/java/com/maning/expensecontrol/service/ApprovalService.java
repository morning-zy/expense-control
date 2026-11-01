package com.maning.expensecontrol.service;

import com.maning.expensecontrol.dto.ApprovalTaskVO;

import java.util.List;

public interface ApprovalService {

    /** 提交审批：校验状态 → 算审批链 → 快照成 approval_task → 单据 DRAFT → APPROVING */
    void submit(Long formId);

    /** 审批通过当前节点；最后一个节点通过则整单 APPROVED */
    void approve(Long formId, Long operatorId, String comment);

    /** 驳回：当前节点 REJECTED，后续节点 SKIPPED，整单 REJECTED */
    void reject(Long formId, Long operatorId, String comment);

    /** 某张单的完整审批进度 */
    List<ApprovalTaskVO> listTasks(Long formId);

    /** 我的待办 */
    List<ApprovalTaskVO> myPending(Long approverId);
}
