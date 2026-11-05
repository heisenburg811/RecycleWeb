package com.recycle.payment;

import com.recycle.order.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟支付：直接返回成功，用于本地开发与演示。
 *
 * <p>不产生真实资金流，trade_no 以 MOCK 开头便于对账时一眼识别。
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    @Override
    public String name() {
        return "mock";
    }

    @Override
    public PaymentResult pay(Order order, String channel) {
        String tradeNo = "MOCK" + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(1000, 9999);
        log.info("模拟支付成功 order={} amount={} tradeNo={}",
                order.getOrderNo(), order.getTotalAmount(), tradeNo);
        return PaymentResult.ok(tradeNo);
    }
}
