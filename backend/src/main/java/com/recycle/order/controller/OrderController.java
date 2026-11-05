package com.recycle.order.controller;

import com.recycle.common.api.R;
import com.recycle.order.dto.OrderQuoteReq;
import com.recycle.order.service.OrderService;
import com.recycle.order.vo.FeeDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单", description = "费用试算（下单/支付/物流预约待接入）")
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
}
