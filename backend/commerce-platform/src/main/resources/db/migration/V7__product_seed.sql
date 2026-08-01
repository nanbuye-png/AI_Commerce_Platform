-- 商品种子数据
-- 提供覆盖电子产品、服装、家居、图书、运动、美妆等品类的商品（ON_SHELF 上架状态）。

INSERT INTO public.product
    (id, product_code, merchant_id, store_id, category_id, product_name, description, brand, status, sales_count, deleted, version, created_time, updated_time)
VALUES
    (10001, 'P202600001', 1, 1, 12, '星耀轻薄本 Pro 14', '14英寸 2.8K 高刷屏，16GB+1TB 大存储，1.29kg 轻薄机身。', '星耀', 'ON_SHELF', 128, false, 0, NOW(), NOW()),
    (10002, 'P202600002', 1, 1, 12, '游戏本 R9-7945HX', 'AMD 锐龙 9 + RTX 4070 显卡，16英寸 240Hz 电竞屏。', '雷神猎影', 'ON_SHELF', 86, false, 0, NOW(), NOW()),
    (10003, 'P202600003', 1, 1, 11, '旗舰影像手机 Pro', '6.7英寸 OLED 曲面屏，旗舰芯片，5000 万像素三摄。', '星耀', 'ON_SHELF', 356, false, 0, NOW(), NOW()),
    (10004, 'P202600004', 1, 1, 11, '轻薄长续航手机 Air', '6.4英寸直屏，5200mAh 大电池，67W 快充。', '星耀', 'ON_SHELF', 502, false, 0, NOW(), NOW()),
    (10005, 'P202600005', 1, 1, 13, '主动降噪蓝牙耳机 Pro', '45dB 深度主动降噪，LDAC 高解析音质，32 小时续航。', '声澈', 'ON_SHELF', 891, false, 0, NOW(), NOW()),
    (10006, 'P202600006', 1, 1, 13, '真无线运动耳机', 'IPX5 防水防汗，蓝牙 5.3 稳定连接。', '声澈', 'ON_SHELF', 234, false, 0, NOW(), NOW()),
    (10007, 'P202600007', 1, 1, 14, '智能手表 GT5', 'AMOLED 大屏，血氧心率监测，100+ 运动模式。', '星耀', 'ON_SHELF', 167, false, 0, NOW(), NOW()),
    (10008, 'P202600008', 1, 1, 21, '纯棉休闲长袖T恤', '100% 精梳棉面料，透气亲肤，经典圆领设计。', '简逸', 'ON_SHELF', 420, false, 0, NOW(), NOW()),
    (10009, 'P202600009', 1, 1, 21, '男士修身牛仔裤', '弹力丹宁面料，修身版型不紧绷。', '简逸', 'ON_SHELF', 315, false, 0, NOW(), NOW()),
    (10010, 'P202600010', 1, 1, 22, '法式碎花收腰连衣裙', '雪纺面料，V领收腰设计，碎花印花。', '花间', 'ON_SHELF', 268, false, 0, NOW(), NOW()),
    (10011, 'P202600011', 1, 1, 22, '宽松针织开衫外套', '软糯针织面料，慵懒宽松版型。', '花间', 'ON_SHELF', 193, false, 0, NOW(), NOW()),
    (10012, 'P202600012', 1, 1, 23, '轻便透气跑步鞋', '飞织鞋面透气轻盈，缓震回弹中底。', '锐步', 'ON_SHELF', 577, false, 0, NOW(), NOW()),
    (10013, 'P202600013', 1, 1, 24, '大容量商务双肩包', '防泼水面料，15.6英寸电脑仓。', '途远', 'ON_SHELF', 145, false, 0, NOW(), NOW()),
    (10014, 'P202600014', 1, 1, 31, '北欧实木床头柜', '橡木实木框架，环保水性漆。', '栖木', 'ON_SHELF', 56, false, 0, NOW(), NOW()),
    (10015, 'P202600015', 1, 1, 33, '100支长绒棉四件套', '100支长绒棉面料，丝滑亲肤。', '安睡', 'ON_SHELF', 89, false, 0, NOW(), NOW()),
    (10016, 'P202600016', 1, 1, 32, '手冲咖啡壶套装', '食品级 304 不锈钢，手冲壶 + 滤杯 + 分享壶。', '慕咖', 'ON_SHELF', 203, false, 0, NOW(), NOW()),
    (10017, 'P202600017', 1, 1, 41, '深入理解计算机系统 第3版', 'CSAPP 经典黑皮书，程序员必读。', '机械工业出版社', 'ON_SHELF', 342, false, 0, NOW(), NOW()),
    (10018, 'P202600018', 1, 1, 42, '三体 全集典藏版', '刘慈欣科幻巨作，雨果奖获奖作品。', '重庆出版社', 'ON_SHELF', 980, false, 0, NOW(), NOW()),
    (10019, 'P202600019', 1, 1, 51, '家用可调节哑铃套装', '单只可调 2.5-32.5kg，家用力量训练。', '力达', 'ON_SHELF', 75, false, 0, NOW(), NOW()),
    (10020, 'P202600020', 1, 1, 52, '户外露营折叠桌椅套装', '铝合金轻量化设计，折叠收纳便携。', '途野', 'ON_SHELF', 64, false, 0, NOW(), NOW()),
    (10021, 'P202600021', 1, 1, 61, '玻尿酸保湿精华液 30ml', '三重玻尿酸分子，深层补水锁水。', '水光', 'ON_SHELF', 456, false, 0, NOW(), NOW()),
    (10022, 'P202600022', 1, 1, 61, '烟酰胺美白淡斑面霜 50g', '5% 烟酰胺精纯配方，淡化痘印色斑。', '水光', 'ON_SHELF', 289, false, 0, NOW(), NOW()),
    (10023, 'P202600023', 1, 1, 62, '丝绒哑光口红', '丝绒质地顺滑不拔干，显白不挑皮。', '颜彩', 'ON_SHELF', 612, false, 0, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO public.product_sku
    (id, product_id, sku_code, attributes_json, price, original_price, weight, status, sales_count, deleted, created_time, updated_time)
VALUES
    (20001, 10001, 'SKU-BOOK-001', '{"颜色":"月光银","版本":"16GB+1TB"}', 6999.00, 7499.00, 1.290, 'ACTIVE', 68, false, NOW(), NOW()),
    (20002, 10001, 'SKU-BOOK-002', '{"颜色":"曜石黑","版本":"16GB+1TB"}', 6999.00, 7499.00, 1.290, 'ACTIVE', 35, false, NOW(), NOW()),
    (20003, 10001, 'SKU-BOOK-003', '{"颜色":"星光灰","版本":"32GB+1TB"}', 7999.00, 8499.00, 1.295, 'ACTIVE', 25, false, NOW(), NOW()),
    (20004, 10002, 'SKU-GAME-001', '{"配置":"RTX4070/32GB/1TB"}', 8499.00, 8999.00, 2.800, 'ACTIVE', 52, false, NOW(), NOW()),
    (20005, 10002, 'SKU-GAME-002', '{"配置":"RTX4080/32GB/2TB"}', 10999.00, 11499.00, 2.850, 'ACTIVE', 34, false, NOW(), NOW()),
    (20006, 10003, 'SKU-PHONE-001', '{"颜色":"星河黑","存储":"12GB+256GB"}', 4999.00, 5299.00, 0.210, 'ACTIVE', 156, false, NOW(), NOW()),
    (20007, 10003, 'SKU-PHONE-002', '{"颜色":"星河黑","存储":"12GB+512GB"}', 5499.00, 5799.00, 0.210, 'ACTIVE', 98, false, NOW(), NOW()),
    (20008, 10003, 'SKU-PHONE-003', '{"颜色":"雾海白","存储":"16GB+1TB"}', 6299.00, 6599.00, 0.215, 'ACTIVE', 102, false, NOW(), NOW()),
    (20009, 10004, 'SKU-AIR-001', '{"颜色":"远山青"}', 2399.00, 2599.00, 0.168, 'ACTIVE', 287, false, NOW(), NOW()),
    (20010, 10004, 'SKU-AIR-002', '{"颜色":"月影银"}', 2399.00, 2599.00, 0.168, 'ACTIVE', 215, false, NOW(), NOW()),
    (20011, 10005, 'SKU-EAR-001', '{"颜色":"星夜黑"}', 899.00, 999.00, 0.060, 'ACTIVE', 456, false, NOW(), NOW()),
    (20012, 10005, 'SKU-EAR-002', '{"颜色":"雪域白"}', 899.00, 999.00, 0.060, 'ACTIVE', 289, false, NOW(), NOW()),
    (20013, 10005, 'SKU-EAR-003', '{"颜色":"薄荷绿"}', 899.00, 999.00, 0.060, 'ACTIVE', 146, false, NOW(), NOW()),
    (20014, 10006, 'SKU-SPT-001', '{"颜色":"黑色"}', 299.00, 349.00, 0.035, 'ACTIVE', 167, false, NOW(), NOW()),
    (20015, 10006, 'SKU-SPT-002', '{"颜色":"蓝色"}', 299.00, 349.00, 0.035, 'ACTIVE', 67, false, NOW(), NOW()),
    (20016, 10007, 'SKU-WATCH-001', '{"颜色":"曜石黑"}', 1299.00, 1499.00, 0.048, 'ACTIVE', 98, false, NOW(), NOW()),
    (20017, 10007, 'SKU-WATCH-002', '{"颜色":"星云白"}', 1299.00, 1499.00, 0.048, 'ACTIVE', 69, false, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO public.product_sku
    (id, product_id, sku_code, attributes_json, price, original_price, weight, status, sales_count, deleted, created_time, updated_time)
VALUES
    (20018, 10008, 'SKU-TEE-001', '{"颜色":"白色","尺码":"L"}', 79.00, 99.00, 0.250, 'ACTIVE', 178, false, NOW(), NOW()),
    (20019, 10008, 'SKU-TEE-002', '{"颜色":"黑色","尺码":"M"}', 79.00, 99.00, 0.250, 'ACTIVE', 142, false, NOW(), NOW()),
    (20020, 10008, 'SKU-TEE-003', '{"颜色":"藏青","尺码":"XL"}', 79.00, 99.00, 0.250, 'ACTIVE', 100, false, NOW(), NOW()),
    (20021, 10009, 'SKU-JEAN-001', '{"颜色":"深蓝","尺码":"32"}', 199.00, 239.00, 0.720, 'ACTIVE', 156, false, NOW(), NOW()),
    (20022, 10009, 'SKU-JEAN-002', '{"颜色":"浅蓝","尺码":"31"}', 199.00, 239.00, 0.720, 'ACTIVE', 89, false, NOW(), NOW()),
    (20023, 10009, 'SKU-JEAN-003', '{"颜色":"黑色","尺码":"30"}', 199.00, 239.00, 0.720, 'ACTIVE', 70, false, NOW(), NOW()),
    (20024, 10010, 'SKU-DRESS-001', '{"颜色":"蓝色碎花","尺码":"M"}', 249.00, 299.00, 0.450, 'ACTIVE', 128, false, NOW(), NOW()),
    (20025, 10010, 'SKU-DRESS-002', '{"颜色":"粉色碎花","尺码":"S"}', 249.00, 299.00, 0.450, 'ACTIVE', 86, false, NOW(), NOW()),
    (20026, 10010, 'SKU-DRESS-003', '{"颜色":"蓝色碎花","尺码":"L"}', 249.00, 299.00, 0.450, 'ACTIVE', 54, false, NOW(), NOW()),
    (20027, 10011, 'SKU-CARD-001', '{"颜色":"燕麦色","尺码":"均码"}', 169.00, 189.00, 0.420, 'ACTIVE', 113, false, NOW(), NOW()),
    (20028, 10011, 'SKU-CARD-002', '{"颜色":"雾霾蓝","尺码":"均码"}', 169.00, 189.00, 0.420, 'ACTIVE', 80, false, NOW(), NOW()),
    (20029, 10012, 'SKU-RUN-001', '{"颜色":"黑武士","尺码":"42"}', 329.00, 399.00, 0.620, 'ACTIVE', 231, false, NOW(), NOW()),
    (20030, 10012, 'SKU-RUN-002', '{"颜色":"白月光","尺码":"41"}', 329.00, 399.00, 0.620, 'ACTIVE', 198, false, NOW(), NOW()),
    (20031, 10012, 'SKU-RUN-003', '{"颜色":"薄荷绿","尺码":"43"}', 329.00, 399.00, 0.620, 'ACTIVE', 148, false, NOW(), NOW()),
    (20032, 10013, 'SKU-BAG-001', '{"颜色":"经典黑"}', 259.00, 299.00, 0.980, 'ACTIVE', 89, false, NOW(), NOW()),
    (20033, 10013, 'SKU-BAG-002', '{"颜色":"藏青蓝"}', 259.00, 299.00, 0.980, 'ACTIVE', 56, false, NOW(), NOW()),
    (20034, 10014, 'SKU-NIGHT-001', '{"颜色":"原木色"}', 399.00, 459.00, 15.000, 'ACTIVE', 32, false, NOW(), NOW()),
    (20035, 10014, 'SKU-NIGHT-002', '{"颜色":"胡桃色"}', 429.00, 489.00, 15.200, 'ACTIVE', 24, false, NOW(), NOW()),
    (20036, 10015, 'SKU-BED-001', '{"颜色":"浅灰"}', 599.00, 699.00, 2.800, 'ACTIVE', 45, false, NOW(), NOW()),
    (20037, 10015, 'SKU-BED-002', '{"颜色":"暖白"}', 599.00, 699.00, 2.800, 'ACTIVE', 44, false, NOW(), NOW()),
    (20038, 10016, 'SKU-COFFEE-001', '{"规格":"600ml"}', 129.00, 159.00, 1.200, 'ACTIVE', 126, false, NOW(), NOW()),
    (20039, 10016, 'SKU-COFFEE-002', '{"规格":"800ml"}', 149.00, 179.00, 1.350, 'ACTIVE', 77, false, NOW(), NOW()),
    (20040, 10017, 'SKU-BOOK-CSAPP-001', '{"版本":"第3版"}', 128.00, 139.00, 1.800, 'ACTIVE', 342, false, NOW(), NOW()),
    (20041, 10018, 'SKU-BOOK-SAN-001', '{"版本":"全集典藏版"}', 168.00, 199.00, 2.100, 'ACTIVE', 980, false, NOW(), NOW()),
    (20042, 10019, 'SKU-DUMB-001', '{"重量":"2.5-32.5kg"}', 369.00, 429.00, 32.500, 'ACTIVE', 48, false, NOW(), NOW()),
    (20043, 10020, 'SKU-CAMP-001', '{"规格":"桌椅套装"}', 329.00, 399.00, 5.800, 'ACTIVE', 39, false, NOW(), NOW()),
    (20044, 10021, 'SKU-HYA-001', '{"规格":"30ml"}', 199.00, 239.00, 0.060, 'ACTIVE', 267, false, NOW(), NOW()),
    (20045, 10021, 'SKU-HYA-002', '{"规格":"50ml"}', 299.00, 349.00, 0.100, 'ACTIVE', 189, false, NOW(), NOW()),
    (20046, 10022, 'SKU-NIAC-001', '{"规格":"50g"}', 159.00, 199.00, 0.070, 'ACTIVE', 178, false, NOW(), NOW()),
    (20047, 10022, 'SKU-NIAC-002', '{"规格":"100g"}', 269.00, 319.00, 0.140, 'ACTIVE', 111, false, NOW(), NOW()),
    (20048, 10023, 'SKU-LIP-001', '{"色号":"#01 豆沙"}', 129.00, 159.00, 0.020, 'ACTIVE', 214, false, NOW(), NOW()),
    (20049, 10023, 'SKU-LIP-002', '{"色号":"#02 砖红"}', 129.00, 159.00, 0.020, 'ACTIVE', 198, false, NOW(), NOW()),
    (20050, 10023, 'SKU-LIP-003', '{"色号":"#03 正红"}', 129.00, 159.00, 0.020, 'ACTIVE', 120, false, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO public.product_spec
    (id, product_id, spec_name, spec_values, sort, deleted, created_time, updated_time)
VALUES
    (30001, 10001, '颜色', '["月光银", "曜石黑", "星光灰"]', 1, false, NOW(), NOW()),
    (30002, 10001, '版本', '["16GB+1TB", "32GB+1TB"]', 2, false, NOW(), NOW()),
    (30003, 10003, '颜色', '["星河黑", "雾海白", "霞光紫"]', 1, false, NOW(), NOW()),
    (30004, 10008, '颜色', '["白色", "黑色", "藏青"]', 1, false, NOW(), NOW()),
    (30005, 10008, '尺码', '["S", "M", "L", "XL"]', 2, false, NOW(), NOW()),
    (30006, 10009, '颜色', '["浅蓝", "深蓝", "黑色"]', 1, false, NOW(), NOW()),
    (30007, 10009, '尺码', '["30", "31", "32", "33"]', 2, false, NOW(), NOW()),
    (30008, 10010, '颜色', '["蓝色碎花", "粉色碎花"]', 1, false, NOW(), NOW()),
    (30009, 10010, '尺码', '["S", "M", "L"]', 2, false, NOW(), NOW()),
    (30010, 10011, '颜色', '["燕麦色", "雾霾蓝", "奶咖色"]', 1, false, NOW(), NOW()),
    (30011, 10011, '尺码', '["均码"]', 2, false, NOW(), NOW()),
    (30012, 10012, '颜色', '["黑武士", "白月光", "薄荷绿"]', 1, false, NOW(), NOW()),
    (30013, 10012, '尺码', '["40", "41", "42", "43"]', 2, false, NOW(), NOW()),
    (30014, 10023, '色号', '["#01 豆沙", "#02 砖红", "#03 正红"]', 1, false, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO public.product_image
    (id, product_id, image_type, url, sort, is_cover, deleted, created_time, updated_time)
VALUES
    (40001, 10001, 'MAIN', 'https://picsum.photos/seed/product-b1/600/600', 1, true, false, NOW(), NOW()),
    (40002, 10002, 'MAIN', 'https://picsum.photos/seed/product-b2/600/600', 1, true, false, NOW(), NOW()),
    (40003, 10003, 'MAIN', 'https://picsum.photos/seed/product-p1/600/600', 1, true, false, NOW(), NOW()),
    (40004, 10004, 'MAIN', 'https://picsum.photos/seed/product-p2/600/600', 1, true, false, NOW(), NOW()),
    (40005, 10005, 'MAIN', 'https://picsum.photos/seed/product-e1/600/600', 1, true, false, NOW(), NOW()),
    (40006, 10006, 'MAIN', 'https://picsum.photos/seed/product-e2/600/600', 1, true, false, NOW(), NOW()),
    (40007, 10007, 'MAIN', 'https://picsum.photos/seed/product-w1/600/600', 1, true, false, NOW(), NOW()),
    (40008, 10008, 'MAIN', 'https://picsum.photos/seed/product-t1/600/600', 1, true, false, NOW(), NOW()),
    (40009, 10009, 'MAIN', 'https://picsum.photos/seed/product-j1/600/600', 1, true, false, NOW(), NOW()),
    (40010, 10010, 'MAIN', 'https://picsum.photos/seed/product-d1/600/600', 1, true, false, NOW(), NOW()),
    (40011, 10011, 'MAIN', 'https://picsum.photos/seed/product-c1/600/600', 1, true, false, NOW(), NOW()),
    (40012, 10012, 'MAIN', 'https://picsum.photos/seed/product-r1/600/600', 1, true, false, NOW(), NOW()),
    (40013, 10013, 'MAIN', 'https://picsum.photos/seed/product-bag/600/600', 1, true, false, NOW(), NOW()),
    (40014, 10014, 'MAIN', 'https://picsum.photos/seed/product-night/600/600', 1, true, false, NOW(), NOW()),
    (40015, 10015, 'MAIN', 'https://picsum.photos/seed/product-bed/600/600', 1, true, false, NOW(), NOW()),
    (40016, 10016, 'MAIN', 'https://picsum.photos/seed/product-coffee/600/600', 1, true, false, NOW(), NOW()),
    (40017, 10017, 'MAIN', 'https://picsum.photos/seed/product-csapp/600/600', 1, true, false, NOW(), NOW()),
    (40018, 10018, 'MAIN', 'https://picsum.photos/seed/product-santi/600/600', 1, true, false, NOW(), NOW()),
    (40019, 10019, 'MAIN', 'https://picsum.photos/seed/product-dumbbell/600/600', 1, true, false, NOW(), NOW()),
    (40020, 10020, 'MAIN', 'https://picsum.photos/seed/product-camp/600/600', 1, true, false, NOW(), NOW()),
    (40021, 10021, 'MAIN', 'https://picsum.photos/seed/product-hya/600/600', 1, true, false, NOW(), NOW()),
    (40022, 10022, 'MAIN', 'https://picsum.photos/seed/product-niacin/600/600', 1, true, false, NOW(), NOW()),
    (40023, 10023, 'MAIN', 'https://picsum.photos/seed/product-lip/600/600', 1, true, false, NOW(), NOW())
ON CONFLICT DO NOTHING;

SELECT setval('public.product_id_seq', (SELECT MAX(id) FROM public.product));
SELECT setval('public.product_sku_id_seq', (SELECT MAX(id) FROM public.product_sku));
SELECT setval('public.product_spec_id_seq', (SELECT MAX(id) FROM public.product_spec));
SELECT setval('public.product_image_id_seq', (SELECT MAX(id) FROM public.product_image));
