package com.maning.expensecontrol.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maning.expensecontrol.dto.ApprovalTaskVO;
import com.maning.expensecontrol.dto.ApproverNode;
import com.maning.expensecontrol.entity.ApprovalRecord;
import com.maning.expensecontrol.entity.ApprovalTask;
import com.maning.expensecontrol.entity.ExpenseForm;
import com.maning.expensecontrol.exception.BusinessException;
import com.maning.expensecontrol.exception.ErrorCode;
import com.maning.expensecontrol.mapper.ApprovalRecordMapper;
import com.maning.expensecontrol.mapper.ApprovalTaskMapper;
import com.maning.expensecontrol.mapper.ExpenseFormMapper;
import com.maning.expensecontrol.mapper.SysEmployeeMapper;
import com.maning.expensecontrol.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    /** 1000 元以内一级审批，10000 元以内两级，超过三级 */
    private static final BigDecimal LEVEL_1_LIMIT = new BigDecimal("1000");
    private static final BigDecimal LEVEL_2_LIMIT = new BigDecimal("10000");

    private final ExpenseFormMapper formMapper;
    private final ApprovalTaskMapper taskMapper;
    private final ApprovalRecordMapper recordMapper;
    private final SysEmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long formId) {
        ExpenseForm form = formMapper.selectById(formId);
        if (form == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报销单不存在：" + formId);
        }

        // 1. 按金额算出需要几级审批，再去组织架构里取链
        int required = requiredNodes(form.getTotalAmount());
        List<ApproverNode> chain = employeeMapper.selectApproverChain(form.getApplicantId(), required);
        if (chain.isEmpty()) {
            // 没有上级就拒绝提交，绝不「自动通过」—— 自动通过等于给了一条绕过审批的后门
            throw new BusinessException(ErrorCode.NO_APPROVER_CONFIGURED,
                    "申请人未配置上级，无法提交审批（金额需 " + required + " 级）");
        }
        // 链可能因为有人离职而比期望的短，以实际链长为准备
        int totalNode = Math.min(required, chain.size());

        // 2. 条件更新抢状态：影响 0 行说明不是草稿（或已被别人提交）
        int affected = formMapper.submitIfDraft(formId, totalNode);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.FORM_STATUS_INVALID,
                    "只有草稿状态的单据可以提交，当前状态：" + form.getStatus());
        }

        // 3. 审批链快照
        for (int i = 0; i < totalNode; i++) {
            ApproverNode node = chain.get(i);
            ApprovalTask task = new ApprovalTask();
            task.setFormId(formId);
            task.setNodeSeq(i + 1);
            task.setNodeName(nodeName(node.getJobLevel(), i + 1));
            task.setApproverId(node.getApproverId());
            task.setStatus("PENDING");
            taskMapper.insert(task);
        }

        // 4. 审计流水
        addRecord(formId, 0, "提交申请", form.getApplicantId(), "SUBMIT",
                "提交审批，金额 " + form.getTotalAmount() + " 元，共 " + totalNode + " 级");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long formId, Long operatorId, String comment) {
        ExpenseForm form = requireApproving(formId);
        int node = form.getCurrentNode();

        ApprovalTask task = currentTask(formId, node);
        checkApprover(formId, task, operatorId);

        // 并发核心：只有「PENDING → APPROVED」这一次能成功
        int affected = taskMapper.decide(formId, node, "APPROVED", comment);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.FORM_STATUS_INVALID, "该节点已被处理，请刷新后重试");
        }
        addRecord(formId, node, task.getNodeName(), operatorId, "APPROVE", comment);

        int updated = (node >= form.getTotalNode())
                ? formMapper.finishApproved(formId, node)      // 最后一级 → 整单通过
                : formMapper.advanceNode(formId, node, node + 1); // 中间节点 → 指针后移

        if (updated == 0) {
            throw new BusinessException(ErrorCode.FORM_STATUS_INVALID, "单据状态已变化，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long formId, Long operatorId, String comment) {
        ExpenseForm form = requireApproving(formId);
        int node = form.getCurrentNode();

        ApprovalTask task = currentTask(formId, node);
        checkApprover(formId, task, operatorId);

        int affected = taskMapper.decide(formId, node, "REJECTED", comment);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.FORM_STATUS_INVALID, "该节点已被处理，请刷新后重试");
        }
        addRecord(formId, node, task.getNodeName(), operatorId, "REJECT", comment);

        // 后面的节点不会再有人处理，明确标成 SKIPPED，避免「待办里一直挂着」
        taskMapper.skipPending(formId);
        formMapper.finishRejected(formId, node);
    }

    @Override
    public List<ApprovalTaskVO> listTasks(Long formId) {
        return taskMapper.selectByFormId(formId);
    }

    @Override
    public List<ApprovalTaskVO> myPending(Long approverId) {
        return taskMapper.selectPendingByApprover(approverId);
    }

    // ==================== 私有辅助 ====================

    private ExpenseForm requireApproving(Long formId) {
        ExpenseForm form = formMapper.selectById(formId);
        if (form == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报销单不存在：" + formId);
        }
        if (!"APPROVING".equals(form.getStatus())) {
            throw new BusinessException(ErrorCode.FORM_STATUS_INVALID,
                    "单据当前状态为 " + form.getStatus() + "，不在审批中");
        }
        return form;
    }

    private ApprovalTask currentTask(Long formId, int nodeSeq) {
        ApprovalTask task = taskMapper.selectOne(new LambdaQueryWrapper<ApprovalTask>()
                .eq(ApprovalTask::getFormId, formId)
                .eq(ApprovalTask::getNodeSeq, nodeSeq));
        if (task == null) {
            throw new BusinessException(ErrorCode.FORM_STATUS_INVALID, "第 " + nodeSeq + " 级审批节点不存在");
        }
        return task;
    }

    private void checkApprover(Long formId, ApprovalTask task, Long operatorId) {
        if (task.getApproverId().equals(operatorId)) {
            return;
        }
        // 并发场景：他刚审完上一级、指针已经推进到下一级 —— 这不算越权，是「重复提交」
        Long handled = taskMapper.selectCount(new LambdaQueryWrapper<ApprovalTask>()
                .eq(ApprovalTask::getFormId, formId)
                .eq(ApprovalTask::getApproverId, operatorId)
                .in(ApprovalTask::getStatus, "APPROVED", "REJECTED"));
        if (handled != null && handled > 0) {
            throw new BusinessException(ErrorCode.FORM_STATUS_INVALID,
                    "你已经处理过该单据，当前进度已流转到「" + task.getNodeName() + "」");
        }
        throw new BusinessException(ErrorCode.NOT_CURRENT_APPROVER,
                "当前节点应由「" + task.getNodeName() + "」处理，你不是该节点审批人");
    }

    private void addRecord(Long formId, int nodeSeq, String nodeName,
                           Long approverId, String action, String comment) {
        ApprovalRecord record = new ApprovalRecord();
        record.setFormId(formId);
        record.setNodeSeq(nodeSeq);
        record.setNodeName(nodeName);
        record.setApproverId(approverId);
        record.setAction(action);
        record.setApprovalComment(comment);
        recordMapper.insert(record);
    }

    /** BigDecimal 比较必须用 compareTo：equals 会把 1000 和 1000.00 判成不相等 */
    private int requiredNodes(BigDecimal amount) {
        if (amount == null) {
            return 1;
        }
        if (amount.compareTo(LEVEL_1_LIMIT) <= 0) {
            return 1;
        }
        return amount.compareTo(LEVEL_2_LIMIT) <= 0 ? 2 : 3;
    }

    private String nodeName(Integer jobLevel, int seq) {
        if (jobLevel == null) {
            return "第 " + seq + " 级审批";
        }
        return switch (jobLevel) {
            case 2 -> "主管审批";
            case 3 -> "部门经理审批";
            case 4 -> "总监审批";
            case 5 -> "总经理审批";
            default -> "第 " + seq + " 级审批";
        };
    }
}
