package com.maning.expensecontrol.service;

import com.maning.expensecontrol.common.PageResult;
import com.maning.expensecontrol.dto.ExpenseFormCreateDTO;
import com.maning.expensecontrol.dto.ExpenseFormDetailVO;
import com.maning.expensecontrol.dto.ExpenseFormListVO;
import com.maning.expensecontrol.dto.ExpenseFormQueryDTO;

public interface ExpenseFormService {
    Long create(ExpenseFormCreateDTO dto);
    /** 分页查询报销单列表 */
    PageResult<ExpenseFormListVO> pageForms(ExpenseFormQueryDTO query);

    /** 查询报销单详情（含明细） */
    ExpenseFormDetailVO getDetail(Long id);


}
