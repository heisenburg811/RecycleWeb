package com.recycle.payment;

/**
 * 支付结果。
 */
public record PaymentResult(boolean success, String tradeNo, String message) {

    public static PaymentResult ok(String tradeNo) {
        return new PaymentResult(true, tradeNo, "支付成功");
    }

    public static PaymentResult fail(String message) {
        return new PaymentResult(false, null, message);
    }
}
