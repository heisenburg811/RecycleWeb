package com.recycle.logistics.task;

import com.recycle.logistics.service.LogisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 到点自动出发：预约时段一开始，订单从「已预约」推进到「配送中」。
 *
 * <p>真实接入运力后，改由第三方回调驱动；这里用定时任务模拟。
 */
@Component
public class LogisticsDispatchTask {

    private static final Logger log = LoggerFactory.getLogger(LogisticsDispatchTask.class);

    private final LogisticsService logisticsService;

    public LogisticsDispatchTask(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @Scheduled(initialDelay = 25_000, fixedDelay = 60_000)
    public void advance() {
        try {
            int n = logisticsService.advanceDue();
            if (n > 0) {
                log.info("推进配送 {} 单", n);
            }
        } catch (Exception e) {
            log.error("配送推进任务异常", e);
        }
    }
}
