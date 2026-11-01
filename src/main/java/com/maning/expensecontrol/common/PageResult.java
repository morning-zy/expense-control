package com.maning.expensecontrol.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;

import java.util.List;

@Getter
public class PageResult<T> {
    private final long page;
    private final long size;
    private final long total;
    private final long pages;
    private final List<T> records;

    public PageResult(IPage<T> p) {
        this.page = p.getCurrent();
        this.size = p.getSize();
        this.total = p.getTotal();
        this.pages = p.getPages();
        this.records = p.getRecords();
    }

}
