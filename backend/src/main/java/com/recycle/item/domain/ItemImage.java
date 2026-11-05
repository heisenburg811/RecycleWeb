package com.recycle.item.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品图片，按 sort 升序，第一张为主图。
 */
@Data
@TableName("item_image")
public class ItemImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;

    private String url;

    private Integer sort;

    public ItemImage() {
    }

    public ItemImage(Long itemId, String url, Integer sort) {
        this.itemId = itemId;
        this.url = url;
        this.sort = sort;
    }
}
