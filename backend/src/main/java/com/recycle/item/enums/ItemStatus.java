package com.recycle.item.enums;

/**
 * 商品状态机（PRD 5.5）。数据库中存字符串，便于阅读与排查。
 */
public enum ItemStatus {

    /** 草稿 */
    DRAFT,
    /** 审核中 */
    PENDING,
    /** 已驳回 */
    REJECTED,
    /** 在售 */
    ON_SALE,
    /** 已预订（下单锁库存） */
    RESERVED,
    /** 已售出 */
    SOLD,
    /** 已下架 */
    OFF_SHELF,
    /** 违规下架 */
    BANNED
}
