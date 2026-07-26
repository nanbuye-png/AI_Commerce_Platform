package com.commerce.platform.common.entity;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 统一分页响应结果
 * 整个项目后续统一使用
 */
@Data
public class PageResult<T> {

    private List<T> list;
    private int page;
    private int size;
    private long total;
    private int pages;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.list = page.getContent();
        result.page = page.getNumber() + 1;
        result.size = page.getSize();
        result.total = page.getTotalElements();
        result.pages = page.getTotalPages();
        return result;
    }
}