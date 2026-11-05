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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品发布与浏览。
 *
 * <p>发布需要登录，卖家从 JWT 解析出的 {@link LoginUser} 取；
 * 浏览类接口匿名可访问（见 SecurityConfig）。
 */
@Tag(name = "商品", description = "发布 / 列表 / 详情")
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

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public R<ItemDetailVO> detail(@PathVariable Long id) {
        return R.ok(itemService.detail(id));
    }
}
