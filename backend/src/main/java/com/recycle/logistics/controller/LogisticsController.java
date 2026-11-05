package com.recycle.logistics.controller;

import com.recycle.common.api.R;
import com.recycle.logistics.dto.BookReq;
import com.recycle.logistics.service.LogisticsService;
import com.recycle.logistics.service.SlotService;
import com.recycle.logistics.vo.LogisticsVO;
import com.recycle.logistics.vo.SlotVO;
import com.recycle.user.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "物流", description = "时段查询 / 预约 / 改约")
@RestController
@RequestMapping("/api/v1")
public class LogisticsController {

    private final SlotService slotService;
    private final LogisticsService logisticsService;

    public LogisticsController(SlotService slotService, LogisticsService logisticsService) {
        this.slotService = slotService;
        this.logisticsService = logisticsService;
    }

    @Operation(summary = "查询某天可预约时段", description = "返回每个时段的容量与余量")
    @GetMapping("/logistics/slots")
    public R<List<SlotVO>> slots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(slotService.list(date));
    }

    @Operation(summary = "查询订单的物流预约")
    @GetMapping("/orders/{orderNo}/logistics")
    public R<LogisticsVO> info(@PathVariable String orderNo,
                               @AuthenticationPrincipal LoginUser user) {
        return R.ok(logisticsService.info(orderNo, user.id()));
    }

    @Operation(summary = "预约配送", description = "订单需已支付且为配送单；时段约满会失败")
    @PostMapping("/orders/{orderNo}/logistics")
    public R<LogisticsVO> book(@PathVariable String orderNo,
                               @Valid @RequestBody BookReq req,
                               @AuthenticationPrincipal LoginUser user) {
        return R.ok(logisticsService.book(orderNo, user.id(), req.date(), req.slotCode()));
    }

    @Operation(summary = "改约配送", description = "配送当日不可改；免费 1 次")
    @PostMapping("/orders/{orderNo}/logistics/reschedule")
    public R<LogisticsVO> reschedule(@PathVariable String orderNo,
                                     @Valid @RequestBody BookReq req,
                                     @AuthenticationPrincipal LoginUser user) {
        return R.ok(logisticsService.reschedule(orderNo, user.id(), req.date(), req.slotCode()));
    }
}
