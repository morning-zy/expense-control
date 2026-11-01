package com.maning.expensecontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("seq_generator")
public class SeqGenerator {
    /** 序列名是主键，不是自增 */
    @TableId(type = IdType.INPUT)
    private String seqName;

    private Long currentVal;

    private LocalDateTime updatedAt;
}
