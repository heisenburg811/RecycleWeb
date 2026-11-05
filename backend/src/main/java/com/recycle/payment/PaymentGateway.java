package com.recycle.payment;

import com.recycle.order.domain.Order;

/**
 * 支付网关抽象。
 *
 * <p>当前只有 {@link MockPaymentGateway}，用来把订单状态机跑通。
 * 真实商户号下来后新增 WechatPaymentGateway 实现本接口即可，
 * OrderService 一行不用改。
 */
public interface PaymentGateway {

    /** 网关标识，落库用 */
    String name();

    /**
     * 发起支付。
     *
     * @param channel wechat / alipay / mock
     */
    PaymentResult pay(Order order, String channel);
}
