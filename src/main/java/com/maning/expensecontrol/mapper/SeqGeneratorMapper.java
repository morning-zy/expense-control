package com.maning.expensecontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maning.expensecontrol.entity.SeqGenerator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeqGeneratorMapper extends BaseMapper<SeqGenerator> {
    /**
     * 原子递增并返回新值。
     * ON DUPLICATE KEY UPDATE 保证「首次插入」和「后续递增」都能一条 SQL 搞定。
     * LAST_INSERT_ID(expr) 是 MySQL 的特殊用法：把 expr 的值存进本连接的 last_insert_id，
     * 之后 SELECT LAST_INSERT_ID() 就能拿到它 —— 这是「让 UPDATE 也能返回新值」的标准技巧。
     */
    @Select("INSERT INTO seq_generator (seq_name, current_val) VALUES (#{name}, LAST_INSERT_ID(1)) "
            + "ON DUPLICATE KEY UPDATE current_val = LAST_INSERT_ID(current_val + 1)")
    void increment(@Param("name") String name);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertId();
}
