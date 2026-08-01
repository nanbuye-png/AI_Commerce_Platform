-- 分类种子数据
-- 提供一级分类（parent_id = 0）与常用二级分类，供 C 端分类树展示与商品筛选。
-- 使用显式 ID 插入，并在末尾将序列推进到最大值，避免后续插入主键冲突。

-- 一级分类
INSERT INTO public.category (id, parent_id, category_name, sort, level, deleted) VALUES
    (1, 0, '电子产品', 1, 1, false),
    (2, 0, '服装', 2, 1, false),
    (3, 0, '家居', 3, 1, false),
    (4, 0, '图书', 4, 1, false),
    (5, 0, '运动', 5, 1, false),
    (6, 0, '美妆', 6, 1, false);

-- 二级分类 - 电子产品
INSERT INTO public.category (id, parent_id, category_name, sort, level, deleted) VALUES
    (11, 1, '手机', 1, 2, false),
    (12, 1, '电脑', 2, 2, false),
    (13, 1, '耳机', 3, 2, false),
    (14, 1, '智能手表', 4, 2, false);

-- 二级分类 - 服装
INSERT INTO public.category (id, parent_id, category_name, sort, level, deleted) VALUES
    (21, 2, '男装', 1, 2, false),
    (22, 2, '女装', 2, 2, false),
    (23, 2, '鞋靴', 3, 2, false),
    (24, 2, '箱包', 4, 2, false);

-- 二级分类 - 家居
INSERT INTO public.category (id, parent_id, category_name, sort, level, deleted) VALUES
    (31, 3, '家具', 1, 2, false),
    (32, 3, '厨具', 2, 2, false),
    (33, 3, '家纺', 3, 2, false);

-- 二级分类 - 图书
INSERT INTO public.category (id, parent_id, category_name, sort, level, deleted) VALUES
    (41, 4, '计算机', 1, 2, false),
    (42, 4, '文学小说', 2, 2, false),
    (43, 4, '少儿读物', 3, 2, false);

-- 二级分类 - 运动
INSERT INTO public.category (id, parent_id, category_name, sort, level, deleted) VALUES
    (51, 5, '健身器材', 1, 2, false),
    (52, 5, '户外装备', 2, 2, false),
    (53, 5, '球类运动', 3, 2, false);

-- 二级分类 - 美妆
INSERT INTO public.category (id, parent_id, category_name, sort, level, deleted) VALUES
    (61, 6, '护肤', 1, 2, false),
    (62, 6, '彩妆', 2, 2, false),
    (63, 6, '香水', 3, 2, false);

-- 推进分类序列，避免后续 JPA 插入主键冲突
SELECT setval('public.category_id_seq', (SELECT MAX(id) FROM public.category));