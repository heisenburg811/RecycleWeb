package com.recycle.item.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.api.PageResult;
import com.recycle.common.exception.BizException;
import com.recycle.item.domain.Item;
import com.recycle.item.domain.ItemImage;
import com.recycle.item.dto.ItemPublishReq;
import com.recycle.item.dto.ItemQuery;
import com.recycle.item.enums.ItemCondition;
import com.recycle.item.enums.ItemStatus;
import com.recycle.item.mapper.ItemImageMapper;
import com.recycle.item.mapper.ItemMapper;
import com.recycle.item.vo.ItemCardVO;
import com.recycle.item.vo.ItemDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 商品领域服务。
 *
 * <p>注意：本期开发环境发布后直接置为 {@link ItemStatus#ON_SALE}（跳过人工审核），
 * 正式接入审核后台后改为 PENDING（PRD 5.4 R3）。
 */
@Service
public class ItemService {

    /** 手机号 / 微信 / QQ，命中则拒绝发布（PRD 5.4 R7） */
    private static final Pattern CONTACT_PATTERN =
            Pattern.compile("(1[3-9]\\d{9})|(微信|威信|vx|VX|qq|QQ|公众号)");

    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;

    public ItemService(ItemMapper itemMapper, ItemImageMapper itemImageMapper) {
        this.itemMapper = itemMapper;
        this.itemImageMapper = itemImageMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long publish(ItemPublishReq req, Long sellerId) {
        if (CONTACT_PATTERN.matcher(req.getTitle()).find()
                || (req.getDescription() != null && CONTACT_PATTERN.matcher(req.getDescription()).find())) {
            throw new BizException("标题或描述中不能填写联系方式");
        }

        Item item = new Item();
        BeanUtils.copyProperties(req, item);
        item.setSellerId(sellerId);
        item.setStatus(ItemStatus.ON_SALE.name());
        item.setRecycleFlag(Boolean.TRUE.equals(req.getApplyRecycle()) ? 1 : 0);
        item.setViewCount(0);
        item.setCoverUrl(req.getImages().get(0));
        itemMapper.insert(item);

        for (int i = 0; i < req.getImages().size(); i++) {
            itemImageMapper.insert(new ItemImage(item.getId(), req.getImages().get(i), i));
        }
        return item.getId();
    }

    public PageResult<ItemCardVO> page(ItemQuery query) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, ItemStatus.ON_SALE.name());

        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Item::getTitle, kw)
                    .or().like(Item::getDescription, kw)
                    .or().like(Item::getBrand, kw));
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Item::getCategoryId, query.getCategoryId());
        }
        if (query.getMinPrice() != null) {
            wrapper.ge(Item::getPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le(Item::getPrice, query.getMaxPrice());
        }
        if (query.getConditions() != null && !query.getConditions().isEmpty()) {
            wrapper.in(Item::getItemCondition, query.getConditions());
        }
        if (query.getTradeType() != null) {
            // 3 表示「皆可」，筛选自提/配送时都要包含它
            wrapper.in(Item::getTradeType, query.getTradeType(), 3);
        }
        if (StringUtils.hasText(query.getMaterial())) {
            wrapper.eq(Item::getMaterial, query.getMaterial());
        }
        if (StringUtils.hasText(query.getStyle())) {
            wrapper.eq(Item::getStyle, query.getStyle());
        }
        if (StringUtils.hasText(query.getDistrictCode())) {
            wrapper.eq(Item::getDistrictCode, query.getDistrictCode());
        }

        switch (query.getSort()) {
            case "price_asc" -> wrapper.orderByAsc(Item::getPrice);
            case "price_desc" -> wrapper.orderByDesc(Item::getPrice);
            default -> wrapper.orderByDesc(Item::getCreatedAt);
        }

        Page<Item> page = new Page<>(query.getPage(), query.getSize());
        Page<Item> result = itemMapper.selectPage(page, wrapper);

        List<ItemCardVO> list = result.getRecords().stream()
                .map(this::toCard)
                .toList();
        return PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public ItemDetailVO detail(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw new BizException("商品不存在");
        }
        // 简单浏览计数，正式版走 Redis 异步累加
        Item update = new Item();
        update.setId(id);
        update.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
        itemMapper.updateById(update);

        List<String> images = itemImageMapper
                .selectList(new LambdaQueryWrapper<ItemImage>()
                        .eq(ItemImage::getItemId, id)
                        .orderByAsc(ItemImage::getSort))
                .stream().map(ItemImage::getUrl).toList();

        return new ItemDetailVO(
                item.getId(), item.getSellerId(), item.getCategoryId(), item.getTitle(),
                item.getDescription(), item.getPrice(), item.getItemCondition(),
                ItemCondition.labelOf(item.getItemCondition()),
                item.getLengthCm(), item.getWidthCm(), item.getHeightCm(),
                item.getMaterial(), item.getStyle(), item.getBrand(),
                item.getTradeType(), item.getDistrictCode(), item.getStatus(),
                images.isEmpty() ? List.of() : images,
                item.getViewCount(), item.getCreatedAt());
    }

    /**
     * 按 ID 取商品实体，供其它模块使用（无副作用；detail() 会累加浏览量）。
     */
    public Item getById(Long id) {
        return itemMapper.selectById(id);
    }

    /**
     * 预占商品：下单时调用，把在售改为已预订，防止并发超卖（PRD AC-16）。
     * 返回受影响行数，0 表示已被别人抢走。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean reserve(Long itemId) {
        Item update = new Item();
        update.setStatus(ItemStatus.RESERVED.name());
        int rows = itemMapper.update(update, new LambdaQueryWrapper<Item>()
                .eq(Item::getId, itemId)
                .eq(Item::getStatus, ItemStatus.ON_SALE.name()));
        return rows > 0;
    }

    private ItemCardVO toCard(Item item) {
        return new ItemCardVO(item.getId(), item.getTitle(), item.getPrice(), item.getCoverUrl(),
                item.getItemCondition(), ItemCondition.labelOf(item.getItemCondition()),
                item.getDistrictCode(), item.getTradeType(), item.getViewCount(), item.getCreatedAt());
    }

    /**
     * 供其它模块批量取用，避免跨包直接访问 Mapper。
     */
    public List<Item> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return itemMapper.selectByIds(ids);
    }
}
