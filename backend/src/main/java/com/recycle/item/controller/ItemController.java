package com.recycle.item.controller;

import com.recycle.common.api.PageResult;
import com.recycle.common.api.R;
import com.recycle.item.dto.ItemPublishReq;
import com.recycle.item.dto.ItemQuery;
import com.recycle.item.service.ItemService;
import com.recycle.item.vo.ItemCardVO;
import com.recycle.item.vo.ItemDetailVO;
import com.recycle.user.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品发布、浏览与管理。
 *
 * <p>浏览类匿名可访问；发布 / 编辑 / 上下架 / 删除需要登录且校验归属。
 */
@Tag(name = "商品", description = "发布 / 列表 / 详情 / 我的发布 / 编辑 / 上下架")
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "发布商品", description = "需登录，seller 取自 token")
    @PostMapping
    public R<Long> publish(@Valid @RequestBody ItemPublishReq req,
                           @AuthenticationPrincipal LoginUser user) {
        return R.ok(itemService.publish(req, user.id()));
    }

    @Operation(summary = "商品列表 / 搜索")
    @GetMapping
    public R<PageResult<ItemCardVO>> page(ItemQuery query) {
        return R.ok(itemService.page(query));
    }

    @Operation(summary = "我的发布", description = "含全部状态；status 可选，如 ON_SALE / OFF_SHELF / SOLD")
    @GetMapping("/mine")
    public R<PageResult<ItemCardVO>> mine(@RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @AuthenticationPrincipal LoginUser user) {
        return R.ok(itemService.myItems(user.id(), status, page, size));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public R<ItemDetailVO> detail(@PathVariable Long id) {
        return R.ok(itemService.detail(id));
    }

    @Operation(summary = "编辑商品", description = "已预订/已售出的商品只允许降价")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @Valid @RequestBody ItemPublishReq req,
                          @AuthenticationPrincipal LoginUser user) {
        itemService.update(id, req, user.id());
        return R.ok();
    }

    @Operation(summary = "上架 / 下架", description = "status = ON_SALE | OFF_SHELF")
    @PatchMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id,
                                @RequestParam String status,
                                @AuthenticationPrincipal LoginUser user) {
        itemService.changeStatus(id, status, user.id());
        return R.ok();
    }

    @Operation(summary = "删除商品", description = "已预订/已售出的不可删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id,
                          @AuthenticationPrincipal LoginUser user) {
        itemService.delete(id, user.id());
        return R.ok();
    }
}
