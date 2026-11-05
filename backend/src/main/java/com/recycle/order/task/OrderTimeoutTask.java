package com.recycle.order.task;

import com.recycle.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 待支付订单超时关单（PRD 7.4：30 分钟）。
 *
 * <p>单实例够用；多实例部署时加分布式锁，或改成延迟队列。
 * 关单动作本身用条件更新，重复执行不会出错。
 */
@Component
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    private final OrderService orderService;

    public OrderTimeoutTask(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 每分钟扫一次；首次延迟 20 秒，避免和应用启动抢资源 */
    @Scheduled(initialDelay = 20_000, fixedDelay = 60_000)
    public void closeExpiredOrders() {
        try {
            int closed = orderService.closeExpiredOrders();
            if (closed > 0) {
                log.info("超时关单 {} 笔", closed);
            }
        } catch (Exception e) {
            log.error("超时关单任务异常", e);
        }
    }
}
