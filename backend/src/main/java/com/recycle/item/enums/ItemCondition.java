package com.recycle.item.enums;

/**
 * 成色（PRD 附录 A）。
 */
public enum ItemCondition {

    NEW(5, "全新未拆封"),
    ALMOST_NEW(4, "几乎全新"),
    LIGHT_WEAR(3, "轻微使用痕迹"),
    OBVIOUS_WEAR(2, "明显使用痕迹"),
    DEFECTIVE(1, "有瑕疵需维修");

    private final int code;
    private final String label;

    ItemCondition(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (ItemCondition c : values()) {
            if (c.code == code) {
                return c.label;
            }
        }
        return "未知成色";
    }
}
