package com.maning.expensecontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maning.expensecontrol.dto.ApprovalTaskVO;
import com.maning.expensecontrol.entity.ApprovalTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApprovalTaskMapper extends BaseMapper<ApprovalTask> {

    /**
     * 处理当前节点：PENDING → APPROVED / REJECTED。
     * <p>
     * 这里是整个审批流并发安全的核心：两个人同时点「通过」时，
     * 只有一个人能把 status 从 PENDING 改走，另一个影响行数为 0。
     * 乐观锁不是必需的 —— 前置状态本身就是锁。
     */
    @Update("""
            UPDATE approval_task
            SET status = #{toStatus}, approval_comment = #{comment}, decided_at = NOW()
            WHERE form_id = #{formId} AND node_seq = #{nodeSeq} AND status = 'PENDING'
            """)
    int decide(@Param("formId") Long formId,
               @Param("nodeSeq") int nodeSeq,
               @Param("toStatus") String toStatus,
               @Param("comment") String comment);

    /** 整单被驳回后，后面还没轮到的节点统一置为 SKIPPED */
    @Update("""
            UPDATE approval_task
            SET status = 'SKIPPED', decided_at = NOW()
            WHERE form_id = #{formId} AND status = 'PENDING'
            """)
    int skipPending(@Param("formId") Long formId);

    /** 某张单的完整审批进度 */
    @Select("""
            SELECT t.id, t.form_id, t.node_seq, t.node_name, t.approver_id, t.status,
                   t.approval_comment, t.decided_at, e.name AS approver_name,
                   f.form_no, a.name AS applicant_name
            FROM approval_task t
            LEFT JOIN sys_employee e ON e.id = t.approver_id
            LEFT JOIN expense_form f ON f.id = t.form_id
            LEFT JOIN sys_employee a ON a.id = f.applicant_id
            WHERE t.form_id = #{formId}
            ORDER BY t.node_seq
            """)
    List<ApprovalTaskVO> selectByFormId(@Param("formId") Long formId);

    /** 我的待办：只查 PENDING，且单据本身还在审批中 */
    @Select("""
            SELECT t.id, t.form_id, t.node_seq, t.node_name, t.approver_id, t.status,
                   t.decided_at, e.name AS approver_name,
                   f.form_no, a.name AS applicant_name
            FROM approval_task t
            JOIN expense_form f ON f.id = t.form_id AND f.deleted = 0 AND f.status = 'APPROVING'
            LEFT JOIN sys_employee e ON e.id = t.approver_id
            LEFT JOIN sys_employee a ON a.id = f.applicant_id
            WHERE t.approver_id = #{approverId} AND t.status = 'PENDING'
            ORDER BY t.created_at ASC
            """)
    List<ApprovalTaskVO> selectPendingByApprover(@Param("approverId") Long approverId);
}
