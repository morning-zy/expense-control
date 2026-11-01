package com.maning.expensecontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maning.expensecontrol.entity.ExpenseItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExpenseItemMapper extends BaseMapper<ExpenseItem> {
}
