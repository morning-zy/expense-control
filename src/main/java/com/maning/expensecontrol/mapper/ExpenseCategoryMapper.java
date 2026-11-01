package com.maning.expensecontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maning.expensecontrol.entity.ExpenseCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExpenseCategoryMapper extends BaseMapper<ExpenseCategory> {
}
