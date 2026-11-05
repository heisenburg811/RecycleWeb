package com.recycle.search.vo;

import java.util.List;

/**
 * 类目节点，支持任意层级（PRD 6.2）。
 */
public record CategoryNode(
        Long id,
        String name,
        String icon,
        List<CategoryNode> children
) {
    public static CategoryNode leaf(Long id, String name) {
        return new CategoryNode(id, name, null, List.of());
    }
}
