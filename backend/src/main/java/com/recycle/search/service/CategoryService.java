package com.recycle.search.service;

import com.recycle.search.vo.CategoryNode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 类目树。
 *
 * <p>MVP 阶段使用静态类目（运营改动频率低）。二期改为 category 表 + 后台配置。
 * <p>ID 规则：一级 1 位，二级 2 位，便于前端判断层级。
 */
@Service
public class CategoryService {

    private static final List<CategoryNode> TREE = List.of(
            new CategoryNode(1L, "沙发座椅", "sofa", List.of(
                    CategoryNode.leaf(11L, "三人沙发"),
                    CategoryNode.leaf(12L, "双人沙发"),
                    CategoryNode.leaf(13L, "单人沙发"),
                    CategoryNode.leaf(14L, "沙发床"),
                    CategoryNode.leaf(15L, "懒人沙发"))),
            new CategoryNode(2L, "床与床垫", "bed", List.of(
                    CategoryNode.leaf(21L, "双人床"),
                    CategoryNode.leaf(22L, "单人床"),
                    CategoryNode.leaf(23L, "床垫"),
                    CategoryNode.leaf(24L, "上下铺"),
                    CategoryNode.leaf(25L, "榻榻米"))),
            new CategoryNode(3L, "桌几", "table", List.of(
                    CategoryNode.leaf(31L, "餐桌"),
                    CategoryNode.leaf(32L, "书桌"),
                    CategoryNode.leaf(33L, "茶几"),
                    CategoryNode.leaf(34L, "边几"),
                    CategoryNode.leaf(35L, "梳妆台"))),
            new CategoryNode(4L, "柜类收纳", "cabinet", List.of(
                    CategoryNode.leaf(41L, "衣柜"),
                    CategoryNode.leaf(42L, "书柜"),
                    CategoryNode.leaf(43L, "电视柜"),
                    CategoryNode.leaf(44L, "鞋柜"),
                    CategoryNode.leaf(45L, "斗柜"))),
            new CategoryNode(5L, "椅凳", "chair", List.of(
                    CategoryNode.leaf(51L, "餐椅"),
                    CategoryNode.leaf(52L, "办公椅"),
                    CategoryNode.leaf(53L, "吧台凳"),
                    CategoryNode.leaf(54L, "长凳"))),
            new CategoryNode(6L, "办公家具", "office", List.of(
                    CategoryNode.leaf(61L, "工位"),
                    CategoryNode.leaf(62L, "会议桌"),
                    CategoryNode.leaf(63L, "文件柜"))),
            new CategoryNode(7L, "其他", "more", List.of(
                    CategoryNode.leaf(71L, "户外家具"),
                    CategoryNode.leaf(72L, "儿童家具"),
                    CategoryNode.leaf(73L, "装饰家具")))
    );

    public List<CategoryNode> tree() {
        return TREE;
    }

    /**
     * 取某个二级类目所属的一级类目 ID，用于「同类商品」推荐。
     */
    public Long parentOf(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        if (categoryId < 10) {
            return categoryId;
        }
        return categoryId / 10;
    }
}
