package com.maning.expensecontrol.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maning.expensecontrol.common.PageResult;
import com.maning.expensecontrol.common.UserContext;
import com.maning.expensecontrol.dto.*;
import com.maning.expensecontrol.entity.*;
import com.maning.expensecontrol.exception.BusinessException;
import com.maning.expensecontrol.exception.ErrorCode;
import com.maning.expensecontrol.mapper.*;
import com.maning.expensecontrol.service.ExpenseFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseFormServiceImpl implements ExpenseFormService {

    private final ExpenseFormMapper formMapper;
    private final ExpenseItemMapper itemMapper;
    private final ExpenseCategoryMapper categoryMapper;
    private final SysEmployeeMapper employeeMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SeqGeneratorMapper seqGeneratorMapper;

    // 申请人取自登录令牌（AuthInterceptor 已校验并放进 UserContext），不再写死

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ExpenseFormCreateDTO dto) {

        // 1. 科目必须存在且启用
        ExpenseCategory category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getStatus() != 1) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 2. 每条明细的金额不能超过科目单笔限额
        for (ExpenseItemDTO item : dto.getItems()) {
            // single_limit 可以是 NULL（DDL 里 NULL 表示不限），必须判空，否则 NPE
            if (category.getSingleLimit() != null
                    && item.getAmount().compareTo(category.getSingleLimit()) > 0) {
                throw new BusinessException(ErrorCode.ITEM_AMOUNT_EXCEED_LIMIT,
                        "单笔金额 " + item.getAmount() + " 超过" + category.getCategoryName()
                                + "限额 " + category.getSingleLimit());
            }
        }

        // 3. 查申请人，拿部门（不信前端传的 deptId）
        Long currentUserId = UserContext.requireUserId();
        SysEmployee applicant = employeeMapper.selectById(currentUserId);
        if (applicant == null) {
            throw new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        // 4. 服务端算总额（不信前端）
        BigDecimal total = dto.getItems().stream()
                .map(ExpenseItemDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. 插入主表
        ExpenseForm form = new ExpenseForm();
        form.setFormNo(generateFormNo());
        form.setApplicantId(currentUserId);
        form.setDeptId(applicant.getDeptId());
        form.setCategoryId(dto.getCategoryId());
        form.setTotalAmount(total);
        form.setItemCount(dto.getItems().size());
        form.setReason(dto.getReason());
        form.setStatus("DRAFT");
        form.setCurrentNode(0);
        form.setTotalNode(0);
        form.setVersion(0);
        form.setDeleted(0);
        formMapper.insert(form);          // 插入后 form.getId() 有值

        // 6. 批量插入明细
        List<ExpenseItem> items = dto.getItems().stream().map(itemDto -> {
            ExpenseItem item = new ExpenseItem();
            item.setFormId(form.getId());    // ← 用回填的 id
            item.setExpenseDate(itemDto.getExpenseDate());
            item.setAmount(itemDto.getAmount());
            item.setDescription(itemDto.getDescription());
            item.setInvoiceNo(itemDto.getInvoiceNo());
            item.setInvoiceCode(itemDto.getInvoiceCode());
            return item;
        }).toList();

        for (ExpenseItem item : items) {
            itemMapper.insert(item);         // 简单版：循环插
        }

        return form.getId();
    }

    @Override
    public PageResult<ExpenseFormListVO> pageForms(ExpenseFormQueryDTO query) {
        Page<ExpenseFormListVO> page = new Page<>(query.getPage(), query.getSize());
        IPage<ExpenseFormListVO> result = formMapper.selectFormPage(page, query);
        return new PageResult<>(result);
    }

    @Override
    public ExpenseFormDetailVO getDetail(Long id) {
        // 第 1 次查：主表（逻辑删除自动过滤 deleted=1）
        ExpenseForm form = formMapper.selectById(id);
        if (form == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "报销单不存在：" + id);
        }

        // 第 2 次查：明细（一次 WHERE form_id = ? 全取）
        List<ExpenseItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<ExpenseItem>()
                        .eq(ExpenseItem::getFormId, id)
                        .orderByAsc(ExpenseItem::getExpenseDate));

        // 字典名：3 次 selectById，但只在详情页，且只有 3 次，不值得为它写 JOIN
        ExpenseFormDetailVO vo = new ExpenseFormDetailVO();
        vo.setId(form.getId());
        vo.setFormNo(form.getFormNo());
        vo.setApplicantId(form.getApplicantId());
        vo.setDeptId(form.getDeptId());
        vo.setCategoryId(form.getCategoryId());
        vo.setTotalAmount(form.getTotalAmount());
        vo.setItemCount(form.getItemCount());
        vo.setReason(form.getReason());
        vo.setStatus(form.getStatus());
        vo.setCurrentNode(form.getCurrentNode());
        vo.setTotalNode(form.getTotalNode());

        SysEmployee emp = employeeMapper.selectById(form.getApplicantId());
        vo.setApplicantName(emp == null ? null : emp.getName());

        SysDepartment dept = departmentMapper.selectById(form.getDeptId());
        vo.setDeptName(dept == null ? null : dept.getDeptName());

        ExpenseCategory cat = categoryMapper.selectById(form.getCategoryId());
        vo.setCategoryName(cat == null ? null : cat.getCategoryName());

        vo.setItems(items.stream().map(it -> {
            ExpenseFormDetailVO.ItemVO iv = new ExpenseFormDetailVO.ItemVO();
            iv.setId(it.getId());
            iv.setExpenseDate(it.getExpenseDate());
            iv.setAmount(it.getAmount());
            iv.setDescription(it.getDescription());
            iv.setInvoiceNo(it.getInvoiceNo());
            iv.setInvoiceCode(it.getInvoiceCode());
            return iv;
        }).toList());

        return vo;
    }

    private String generateFormNo() {
        String period = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String seqName = "BX" + period;

        seqGeneratorMapper.increment(seqName);          // 原子递增
        Long seq = seqGeneratorMapper.lastInsertId();   // 取回本连接刚拿到的号

        return seqName + String.format("%04d", seq);
    }
}
