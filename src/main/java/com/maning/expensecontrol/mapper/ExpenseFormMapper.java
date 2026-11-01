package com.maning.expensecontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maning.expensecontrol.dto.ExpenseFormListVO;
import com.maning.expensecontrol.dto.ExpenseFormQueryDTO;
import com.maning.expensecontrol.entity.ExpenseForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ExpenseFormMapper extends BaseMapper<ExpenseForm> {
    @Select("SELECT MAX(form_no) FROM expense_form WHERE form_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxFormNo(@Param("prefix") String prefix);

    IPage<ExpenseFormListVO> selectFormPage(IPage<ExpenseFormListVO> page,
                                            @Param("q") ExpenseFormQueryDTO q);

    // ==================== 状态流转：全部用「带前置条件的 UPDATE」 ====================
    // 判断条件写进 WHERE，再看影响行数：
    //   1 行 = 抢到了，继续往下走
    //   0 行 = 状态已被别人改过，直接拒绝
    // 这比「先 select 查状态、再 update」安全 —— 后者是典型的先查后改竞态
    // （3-3 的单号重复就是同一类错误，只是那次的「共享变量」是 MAX(form_no)）。

    /** DRAFT → APPROVING，同时写入审批链长度与提交时间 */
    @Update("""
            UPDATE expense_form
            SET status = 'APPROVING', current_node = 1, total_node = #{totalNode}, submit_time = NOW()
            WHERE id = #{id} AND status = 'DRAFT' AND deleted = 0
            """)
    int submitIfDraft(@Param("id") Long id, @Param("totalNode") int totalNode);

    /** 当前节点通过后，指针后移 */
    @Update("""
            UPDATE expense_form
            SET current_node = #{nextNode}
            WHERE id = #{id} AND status = 'APPROVING' AND current_node = #{currentNode} AND deleted = 0
            """)
    int advanceNode(@Param("id") Long id,
                    @Param("currentNode") int currentNode,
                    @Param("nextNode") int nextNode);

    /** 最后一个节点通过 → 整单通过 */
    @Update("""
            UPDATE expense_form
            SET status = 'APPROVED', finish_time = NOW()
            WHERE id = #{id} AND status = 'APPROVING' AND current_node = #{currentNode} AND deleted = 0
            """)
    int finishApproved(@Param("id") Long id, @Param("currentNode") int currentNode);

    /** 任一节点驳回 → 整单驳回 */
    @Update("""
            UPDATE expense_form
            SET status = 'REJECTED', finish_time = NOW()
            WHERE id = #{id} AND status = 'APPROVING' AND current_node = #{currentNode} AND deleted = 0
            """)
    int finishRejected(@Param("id") Long id, @Param("currentNode") int currentNode);
}
