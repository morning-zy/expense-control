package com.maning.expensecontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maning.expensecontrol.dto.ApproverNode;
import com.maning.expensecontrol.entity.SysEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysEmployeeMapper extends BaseMapper<SysEmployee> {

    /**
     * 从申请人的「直属上级」开始，沿 manager_id 向上爬，取前 maxDepth 级。
     * <p>
     * 一条递归 CTE 拿到整条审批链：不写它就得在 Java 里循环 selectById，
     * 每爬一级一次数据库往返，而且没有上限保护（配置成环会死循环）。
     * <p>
     * 起点是 a.manager_id 而不是 a.id —— 申请人自己不是审批人。
     */
    @Select("""
            WITH RECURSIVE chain AS (
                SELECT m.id, m.name, m.job_level, m.manager_id, 1 AS node_seq
                FROM sys_employee a
                JOIN sys_employee m ON m.id = a.manager_id
                WHERE a.id = #{applicantId} AND m.status = 1
                UNION ALL
                SELECT m2.id, m2.name, m2.job_level, m2.manager_id, c.node_seq + 1
                FROM sys_employee m2
                JOIN chain c ON m2.id = c.manager_id
                WHERE c.node_seq < #{maxDepth} AND m2.status = 1
            )
            SELECT id           AS approverId,
                   name         AS approverName,
                   job_level    AS jobLevel,
                   node_seq     AS nodeSeq
            FROM chain
            ORDER BY node_seq
            """)
    List<ApproverNode> selectApproverChain(@Param("applicantId") Long applicantId,
                                           @Param("maxDepth") int maxDepth);
}
