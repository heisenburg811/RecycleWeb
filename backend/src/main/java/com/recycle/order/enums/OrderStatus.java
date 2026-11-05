package com.recycle.order.enums;

/**
 * 订单状态机（PRD 7.5 的 MVP 子集）。
 *
 * <pre>
 * 配送单：PENDING_PAYMENT ─支付→ PAID ─预约时段→ SCHEDULED ─到点→ DELIVERING ─确认收货→ COMPLETED
 * 自提单：PENDING_PAYMENT ─支付→ PAID ──────────买家确认收货──────────→ COMPLETED
 *                   │
 *                   ├─买家取消→ CANCELLED（释放商品）
 *                   └─30 分钟超时→ CLOSED（释放商品）
 * </pre>
 */
public enum OrderStatus {

    PENDING_PAYMENT("待支付"),
    PAID("待预约配送"),
    SCHEDULED("已预约"),
    DELIVERING("配送中"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    CLOSED("已关闭");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(String code) {
        if (code == null) {
            return "";
        }
        for (OrderStatus s : values()) {
            if (s.name().equals(code)) {
                return s.label;
            }
        }
        return code;
    }
}
