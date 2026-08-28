package com.commerce.platform.common.config;

import com.commerce.platform.product.entity.Category;
import com.commerce.platform.product.entity.Product;
import com.commerce.platform.product.entity.ProductSku;
import com.commerce.platform.product.enums.ProductStatus;
import com.commerce.platform.product.repository.CategoryRepository;
import com.commerce.platform.product.repository.ProductRepository;
import com.commerce.platform.user.entity.User;
import com.commerce.platform.user.enums.UserRole;
import com.commerce.platform.user.enums.UserStatus;
import com.commerce.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.demo", name = "seed-enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "Demo123!";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database is not empty; skipping demo seed");
            return;
        }

        User customer = createUser("demo_customer", "customer@demo.local", "演示顾客", UserRole.CUSTOMER);
        User merchant = createUser("demo_merchant", "merchant@demo.local", "演示商家", UserRole.MERCHANT);
        User admin = createUser("demo_admin", "admin@demo.local", "演示管理员", UserRole.ADMIN);

        if (productRepository.findByProductCode("DEMO-DIGITAL-001").isPresent()) {
            log.info("Demo catalog already exists; skipping catalog seed");
            return;
        }

        Category digital = createCategory("数码家电", 1);
        Category home = createCategory("家居生活", 2);
        Category sport = createCategory("运动户外", 3);

        List<DemoProduct> products = List.of(
                new DemoProduct("DEMO-DIGITAL-001", "星云降噪蓝牙耳机", "Aurora", digital, "699.00", 48, 320),
                new DemoProduct("DEMO-DIGITAL-002", "云雀开放式运动耳机", "Skylark", digital, "899.00", 36, 210),
                new DemoProduct("DEMO-DIGITAL-003", "极光头戴式旗舰耳机", "Aurora", digital, "999.00", 20, 180),
                new DemoProduct("DEMO-DIGITAL-004", "轻薄办公笔记本电脑", "Nova", digital, "4999.00", 15, 95),
                new DemoProduct("DEMO-DIGITAL-005", "机械键盘 87 键", "KeyLab", digital, "399.00", 65, 420),
                new DemoProduct("DEMO-HOME-001", "人体工学办公椅", "Comfort", home, "1299.00", 12, 76),
                new DemoProduct("DEMO-HOME-002", "智能护眼台灯", "LightUp", home, "269.00", 50, 260),
                new DemoProduct("DEMO-HOME-003", "便携咖啡机", "BrewGo", home, "499.00", 28, 144),
                new DemoProduct("DEMO-HOME-004", "乳胶护颈枕", "SleepWell", home, "199.00", 80, 510),
                new DemoProduct("DEMO-SPORT-001", "专业瑜伽垫", "MoveOn", sport, "159.00", 90, 620),
                new DemoProduct("DEMO-SPORT-002", "可调节哑铃套装", "PowerFit", sport, "799.00", 24, 205),
                new DemoProduct("DEMO-SPORT-003", "轻量露营帐篷", "WildCamp", sport, "1199.00", 18, 132)
        );

        for (DemoProduct item : products) {
            createProduct(item, merchant.getId());
        }
        log.info("Demo data initialized: customer={}, merchant={}, admin={}, products={}",
                customer.getUsername(), merchant.getUsername(), admin.getUsername(), products.size());
    }

    private User createUser(String username, String email, String nickname, UserRole role) {
        return userRepository.findByUsername(username).orElseGet(() -> userRepository.save(User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .nickname(nickname)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build()));
    }

    private Category createCategory(String name, int sort) {
        return categoryRepository.findByParentIdOrderBySortAsc(0L).stream()
                .filter(category -> category.getCategoryName().equals(name))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .parentId(0L)
                        .categoryName(name)
                        .sort(sort)
                        .level(1)
                        .deleted(false)
                        .build()));
    }

    private void createProduct(DemoProduct item, Long merchantId) {
        Product product = Product.builder()
                .productCode(item.code())
                .merchantId(merchantId)
                .storeId(merchantId)
                .categoryId(item.category().getId())
                .productName(item.name())
                .description("演示商品：" + item.name() + "。用于 AI 商品搜索、价格过滤和安全加购闭环。")
                .brand(item.brand())
                .status(ProductStatus.ON_SHELF)
                .salesCount(item.sales())
                .deleted(false)
                .build();
        ProductSku sku = ProductSku.builder()
                .product(product)
                .skuCode(item.code() + "-DEFAULT")
                .attributesJson("{\"规格\":\"标准版\"}")
                .price(new BigDecimal(item.price()))
                .originalPrice(new BigDecimal(item.price()).add(new BigDecimal("100.00")))
                .status("ACTIVE")
                .salesCount(item.sales())
                .deleted(false)
                .build();
        product.getSkus().add(sku);
        Product saved = productRepository.save(product);
        ProductSku savedSku = saved.getSkus().getFirst();
        jdbcTemplate.update("""
                INSERT INTO inventory
                    (product_id, sku_id, available_stock, reserved_stock, locked_stock, sold_stock, status, created_time, updated_time)
                SELECT ?, ?, ?, 0, 0, ?, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                WHERE NOT EXISTS (SELECT 1 FROM inventory WHERE sku_id = ?)
                """, saved.getId(), savedSku.getId(), item.stock(), item.sales(), savedSku.getId());
    }

    private record DemoProduct(
            String code,
            String name,
            String brand,
            Category category,
            String price,
            int stock,
            int sales
    ) {
    }
}
