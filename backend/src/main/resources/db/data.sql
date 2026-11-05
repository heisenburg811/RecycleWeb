-- 开发环境演示数据（H2 内存库，每次启动重建）
-- 注意：演示用户不在 SQL 里建，由 DevUserInitializer 用 BCrypt 加密后写入。

INSERT INTO item (id, seller_id, category_id, title, description, item_condition, price,
                  length_cm, width_cm, height_cm, material, style, brand,
                  trade_type, district_code, address, cover_url, status, view_count) VALUES
 (1, 1, 11, '北欧布艺三人沙发 浅灰色 几乎全新',
    '去年购入，用了不到半年，因换房尺寸不合适。坐感偏软，可拆洗，无破损。仅支持自提，楼层有电梯。',
    4, 1280.00, 220, 90, 85, '布艺', '北欧', 'HAY', 1, '110105', '朝阳区望京 SOHO 附近', 'https://picsum.photos/seed/sofa1/600/600', 'ON_SALE', 156),
 (2, 1, 21, '实木双人床 1.8 米 带床头柜',
    '白蜡木，使用两年，有轻微使用痕迹，结构稳固。可拆装，需自备工具或加购拆装服务。',
    3, 2600.00, 200, 180, 100, '实木', '日式', '无印良品', 3, '110105', '朝阳区望京', 'https://picsum.photos/seed/bed1/600/600', 'ON_SALE', 89),
 (3, 2, 31, '橡木餐桌 1.4 米 可坐四人',
    '搬家出，桌面有一处浅划痕，已拍照。配送需加运费，可协助搬下楼。',
    3, 850.00, 140, 80, 75, '实木', '现代简约', NULL, 2, '110108', '海淀区中关村', 'https://picsum.photos/seed/table1/600/600', 'ON_SALE', 203),
 (4, 2, 41, '两门衣柜 白色 板式',
    '租房留下的，成色一般，柜门有一点点变形但能正常开关。低价出，自提优先。',
    2, 320.00, 120, 60, 200, '板式人造板', '现代简约', NULL, 1, '110108', '海淀区五道口', 'https://picsum.photos/seed/wardrobe1/600/600', 'ON_SALE', 47),
 (5, 1, 52, '人体工学办公椅 网面透气',
    '腰托可调，气压杆正常。因为公司搬新工位出掉，成色很好。',
    4, 480.00, 70, 70, 120, '金属', '现代简约', '西昊', 3, '110105', '朝阳区国贸', 'https://picsum.photos/seed/chair1/600/600', 'ON_SALE', 312),
 (6, 1, 33, '岩板茶几 圆形 直径 80',
    '岩板台面 + 金属腿，无磕碰。适合小户型客厅，重量较大，建议平台配送。',
    5, 660.00, 80, 80, 42, '玻璃石材', '轻奢', NULL, 2, '110105', '朝阳区双井', 'https://picsum.photos/seed/tea1/600/600', 'ON_SALE', 71),
 (7, 2, 13, '单人沙发 懒人椅 藤编',
    '阳台用，藤编坐面有一点松散，不影响使用。', 2, 150.00, 75, 75, 90, '藤编', '复古', NULL, 1, '110108', '海淀区学院路', 'https://picsum.photos/seed/chair2/600/600', 'ON_SALE', 28),
 (8, 1, 42, '五层书柜 开放格 实木贴皮',
    '搬家清出，层板承重正常。有轻微磕角，已拍细节图。', 3, 390.00, 80, 30, 180, '板式人造板', '北欧', NULL, 3, '110105', '朝阳区三里屯', 'https://picsum.photos/seed/shelf1/600/600', 'ON_SALE', 64);

INSERT INTO item_image (item_id, url, sort) VALUES
 (1, 'https://picsum.photos/seed/sofa1/600/600', 0),
 (1, 'https://picsum.photos/seed/sofa1b/600/600', 1),
 (2, 'https://picsum.photos/seed/bed1/600/600', 0),
 (3, 'https://picsum.photos/seed/table1/600/600', 0),
 (4, 'https://picsum.photos/seed/wardrobe1/600/600', 0),
 (5, 'https://picsum.photos/seed/chair1/600/600', 0),
 (6, 'https://picsum.photos/seed/tea1/600/600', 0),
 (7, 'https://picsum.photos/seed/chair2/600/600', 0),
 (8, 'https://picsum.photos/seed/shelf1/600/600', 0);

-- 上面用显式 ID 灌数据，H2 的自增序列不会自动推进，
-- 这里手动把序列推到 100，否则新发布的数据会撞主键。
ALTER TABLE item ALTER COLUMN id RESTART WITH 100;
ALTER TABLE item_image ALTER COLUMN id RESTART WITH 100;

