package com.recycle.search.controller;

import com.recycle.common.api.PageResult;
import com.recycle.common.api.R;
import com.recycle.item.dto.ItemQuery;
import com.recycle.item.service.ItemService;
import com.recycle.item.vo.ItemCardVO;
import com.recycle.search.service.CategoryService;
import com.recycle.search.vo.CategoryNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 类目与搜索。
 *
 * <p>搜索本身复用 ItemService 的查询能力——单体内部的「跨模块调用」走 Service，
 * 不直接碰对方 Mapper。
 */
@Tag(name = "搜索与分类")
@RestController
@RequestMapping("/api/v1")
public class SearchController {

    private final CategoryService categoryService;
    private final ItemService itemService;

    public SearchController(CategoryService categoryService, ItemService itemService) {
        this.categoryService = categoryService;
        this.itemService = itemService;
    }

    @Operation(summary = "类目树")
    @GetMapping("/categories")
    public R<List<CategoryNode>> categories() {
        return R.ok(categoryService.tree());
    }

    @Operation(summary = "搜索商品", description = "与 /items 同参数，独立出路径便于后续接 ES")
    @GetMapping("/search")
    public R<PageResult<ItemCardVO>> search(ItemQuery query) {
        return R.ok(itemService.page(query));
    }

    @Operation(summary = "热搜词（运营配置，本期静态）")
    @GetMapping("/search/hot")
    public R<List<String>> hot() {
        return R.ok(List.of("沙发", "双人床", "餐桌", "衣柜", "办公椅", "书桌", "茶几", "床垫"));
    }
}
