package com.recycle.order.controller;

import com.recycle.common.api.PageResult;
import com.recycle.common.api.R;
import com.recycle.order.dto.CreateOrderReq;
import com.recycle.order.dto.OrderQuoteReq;
import com.recycle.order.dto.PayReq;
import com.recycle.order.service.OrderService;
import com.recycle.order.vo.FeeDetailVO;
import com.recycle.order.vo.OrderVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单", description = "试算 / 下单 / 支付 / 发货 / 确认收货 / 列表")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "下单前费用试算", description = "返回商品价 / 运费 / 楼层费 / 拆装费 / 服务费明细")
    @PostMapping("/quote")
    public R<FeeDetailVO> quote(@Valid @RequestBody OrderQuoteReq req) {
        return R.ok(orderService.quote(req));
    }

    @Operation(summary = "创建订单", description = "锁商品库存 → 快照金额 → 待支付（30 分钟）")
    @PostMapping
    public R<OrderVO> create(@Valid @RequestBody CreateOrderReq req,
                             @AuthenticationPrincipal LoginUser user) {
        return R.ok(orderService.create(req, user.id()));
    }

    @Operation(summary = "支付", description = "当前为模拟支付网关，接入真实商户号后替换实现")
    @PostMapping("/{orderNo}/pay")
    public R<OrderVO> pay(@PathVariable String orderNo,
                          @RequestBody(required = false) PayReq req,
                          @AuthenticationPrincipal LoginUser user) {
        return R.ok(orderService.pay(orderNo, user.id(), req == null ? null : req.channel()));
    }

    @Operation(summary = "确认收货（买家）", description = "配送单需先到配送中；自提单付款后即可确认")
    @PostMapping("/{orderNo}/confirm")
    public R<OrderVO> confirm(@PathVariable String orderNo,
                              @AuthenticationPrincipal LoginUser user) {
        return R.ok(orderService.confirm(orderNo, user.id()));
    }

    @Operation(summary = "取消订单（买家，仅未支付）")
    @PostMapping("/{orderNo}/cancel")
    public R<OrderVO> cancel(@PathVariable String orderNo,
                             @AuthenticationPrincipal LoginUser user) {
        return R.ok(orderService.cancel(orderNo, user.id()));
    }

    @Operation(summary = "我的订单", description = "role=buyer 我买到的 / role=seller 我卖出的")
    @GetMapping
    public R<PageResult<OrderVO>> page(@RequestParam(defaultValue = "buyer") String role,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @AuthenticationPrincipal LoginUser user) {
        return R.ok(orderService.page(role, status, page, size, user.id()));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderNo}")
    public R<OrderVO> detail(@PathVariable String orderNo,
                             @AuthenticationPrincipal LoginUser user) {
        return R.ok(orderService.detail(orderNo, user.id()));
    }
}
