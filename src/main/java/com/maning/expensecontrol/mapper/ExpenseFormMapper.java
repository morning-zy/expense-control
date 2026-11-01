package com.maning.expensecontrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maning.expensecontrol.dto.ExpenseFormListVO;
import com.maning.expensecontrol.dto.ExpenseFormQueryDTO;
import com.maning.expensecontrol.entity.ExpenseForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExpenseFormMapper extends BaseMapper<ExpenseForm> {
    @Select("SELECT MAX(form_no) FROM expense_form WHERE form_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxFormNo(@Param("prefix") String prefix);

    IPage<ExpenseFormListVO> selectFormPage(IPage<ExpenseFormListVO> page,
                                            @Param("q") ExpenseFormQueryDTO q);
}
