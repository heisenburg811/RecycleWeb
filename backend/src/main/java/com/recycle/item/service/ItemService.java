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
     * 释放商品：订单取消或超时关单时调用，把已预订改回在售。
     * 带状态条件，避免把已售出的商品错误地放回货架。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean release(Long itemId) {
        Item update = new Item();
        update.setStatus(ItemStatus.ON_SALE.name());
        int rows = itemMapper.update(update, new LambdaQueryWrapper<Item>()
                .eq(Item::getId, itemId)
                .eq(Item::getStatus, ItemStatus.RESERVED.name()));
        return rows > 0;
    }

    /**
     * 标记售出：订单完成后调用。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean markSold(Long itemId) {
        Item update = new Item();
        update.setStatus(ItemStatus.SOLD.name());
        int rows = itemMapper.update(update, new LambdaQueryWrapper<Item>()
                .eq(Item::getId, itemId)
                .eq(Item::getStatus, ItemStatus.RESERVED.name()));
        return rows > 0;
    }

    /**
     * 我发布的商品（全部状态，倒序）。
     */
    public PageResult<ItemCardVO> myItems(Long sellerId, String status, int page, int size) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getSellerId, sellerId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Item::getStatus, status);
        }
        wrapper.orderByDesc(Item::getCreatedAt);

        Page<Item> result = itemMapper.selectPage(new Page<>(page, Math.min(size, 50)), wrapper);
        return PageResult.of(result.getRecords().stream().map(this::toCard).toList(),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 编辑商品。
     *
     * <p>已预订 / 已售出的商品（PRD 5.4 R6）只允许降价，其余字段冻结——
     * 否则买家看到的商品和下单时的不一致。
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ItemPublishReq req, Long sellerId) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw new BizException("商品不存在");
        }
        if (!item.getSellerId().equals(sellerId)) {
            throw new BizException("无权操作该商品");
        }

        boolean locked = ItemStatus.RESERVED.name().equals(item.getStatus())
                || ItemStatus.SOLD.name().equals(item.getStatus());
        if (locked) {
            if (req.getPrice() == null || req.getPrice().compareTo(item.getPrice()) > 0) {
                throw new BizException("商品已被下单，只能降低价格");
            }
            if (contentChanged(item, req)) {
                throw new BizException("商品已被下单，只能修改价格");
            }
            Item priceOnly = new Item();
            priceOnly.setId(id);
            priceOnly.setPrice(req.getPrice());
            itemMapper.updateById(priceOnly);
            return;
        }

        Item update = new Item();
        update.setId(id);
        update.setTitle(req.getTitle());
        update.setDescription(req.getDescription());
        update.setCategoryId(req.getCategoryId());
        update.setItemCondition(req.getItemCondition());
        update.setPrice(req.getPrice());
        update.setLengthCm(req.getLengthCm());
        update.setWidthCm(req.getWidthCm());
        update.setHeightCm(req.getHeightCm());
        update.setMaterial(req.getMaterial());
        update.setStyle(req.getStyle());
        update.setBrand(req.getBrand());
        update.setTradeType(req.getTradeType());
        update.setDistrictCode(req.getDistrictCode());
        update.setAddress(req.getAddress());
        update.setCoverUrl(req.getImages().get(0));
        itemMapper.updateById(update);

        itemImageMapper.delete(new LambdaQueryWrapper<ItemImage>().eq(ItemImage::getItemId, id));
        for (int i = 0; i < req.getImages().size(); i++) {
            itemImageMapper.insert(new ItemImage(id, req.getImages().get(i), i));
        }
    }

    /**
     * 下架 / 重新上架。只允许在售与已下架之间切换。
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, String target, Long sellerId) {
        if (!ItemStatus.ON_SALE.name().equals(target) && !ItemStatus.OFF_SHELF.name().equals(target)) {
            throw new BizException("只支持上架或下架操作");
        }
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw new BizException("商品不存在");
        }
        if (!item.getSellerId().equals(sellerId)) {
            throw new BizException("无权操作该商品");
        }
        Item update = new Item();
        update.setStatus(target);
        int rows = itemMapper.update(update, new LambdaQueryWrapper<Item>()
                .eq(Item::getId, id)
                .in(Item::getStatus, ItemStatus.ON_SALE.name(), ItemStatus.OFF_SHELF.name()));
        if (rows == 0) {
            throw new BizException("当前状态不可变更，请刷新后重试");
        }
    }

    /**
     * 删除商品。已预订 / 已售出的不能删，避免订单指向空数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long sellerId) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw new BizException("商品不存在");
        }
        if (!item.getSellerId().equals(sellerId)) {
            throw new BizException("无权操作该商品");
        }
        if (ItemStatus.RESERVED.name().equals(item.getStatus())
                || ItemStatus.SOLD.name().equals(item.getStatus())) {
            throw new BizException("商品已被下单，不能删除");
        }
        itemImageMapper.delete(new LambdaQueryWrapper<ItemImage>().eq(ItemImage::getItemId, id));
        itemMapper.deleteById(id);
    }

    /**
     * 判断除价格外的内容是否变化（锁定期用）。
     */
    private boolean contentChanged(Item item, ItemPublishReq req) {
        return !java.util.Objects.equals(item.getTitle(), req.getTitle())
                || !java.util.Objects.equals(item.getDescription(), req.getDescription())
                || !java.util.Objects.equals(item.getCategoryId(), req.getCategoryId())
                || !java.util.Objects.equals(item.getItemCondition(), req.getItemCondition())
                || !java.util.Objects.equals(item.getTradeType(), req.getTradeType())
                || !java.util.Objects.equals(item.getCoverUrl(), req.getImages().get(0));
    }

    /**
     * 供其它模块按 ID 取商品实体（不产生副作用，detail 会累加浏览量）。
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
                item.getDistrictCode(), item.getTradeType(), item.getViewCount(), item.getCreatedAt(),
                item.getStatus());
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
