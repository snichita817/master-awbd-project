package com.awbd.financetracker.config;

import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.entity.PaymentMethod;
import com.awbd.financetracker.entity.Role;
import com.awbd.financetracker.entity.Subscription;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.BillingFrequency;
import com.awbd.financetracker.enums.PaymentType;
import com.awbd.financetracker.repository.CategoryRepository;
import com.awbd.financetracker.repository.PaymentMethodRepository;
import com.awbd.financetracker.repository.RoleRepository;
import com.awbd.financetracker.repository.SubscriptionRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SubscriptionRepository subscriptionRepository;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           CategoryRepository categoryRepository,
                           PaymentMethodRepository paymentMethodRepository,
                           SubscriptionRepository subscriptionRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        User admin;
        if (!userRepository.existsByEmail("admin@financetracker.com")) {
            admin = new User("Admin", "admin@financetracker.com", BigDecimal.ZERO);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(List.of(userRole, adminRole));
            admin = userRepository.save(admin);
        } else {
            admin = userRepository.findByEmail("admin@financetracker.com").orElseThrow();
        }

        seedAdminData(admin);
    }

    private void seedAdminData(User admin) {
        Long uid = admin.getId();

        // Categories (skip if already exists by name)
        Category streaming = getOrCreateCategory("Streaming Services", "Video and music streaming platforms", admin, uid);
        Category software = getOrCreateCategory("Software & Tools", "Developer tools, productivity apps and cloud IDEs", admin, uid);
        Category cloudStore = getOrCreateCategory("Cloud Storage", "Online backup and file-sync services", admin, uid);
        Category gaming = getOrCreateCategory("Gaming", "Game subscription and online gaming services", admin, uid);
        Category health = getOrCreateCategory("Health & Fitness", "Gym memberships, fitness and wellness apps", admin, uid);
        Category news = getOrCreateCategory("News & Media", "Digital newspapers, magazines and podcasts", admin, uid);
        Category education = getOrCreateCategory("Education", "Online learning and e-book platforms", admin, uid);
        Category food = getOrCreateCategory("Food & Delivery", "Meal kits and food delivery memberships", admin, uid);

        // Payment methods (skip if already exists by details)
        PaymentMethod visa  = getOrCreatePaymentMethod(PaymentType.CREDIT_CARD, "Visa ending in 4242", admin);
        PaymentMethod paypal = getOrCreatePaymentMethod(PaymentType.PAYPAL, "admin@financetracker.com", admin);
        PaymentMethod mastercard = getOrCreatePaymentMethod(PaymentType.DEBIT_CARD, "Mastercard ending in 5555", admin);

        // Subscriptions (skip if already exists by name)
        LocalDate today = LocalDate.now();

        saveSubscriptionIfAbsent("Netflix", new BigDecimal("15.99"), BillingFrequency.MONTHLY, today.plusDays(12), admin, streaming, visa);
        saveSubscriptionIfAbsent("Spotify", new BigDecimal("10.99"), BillingFrequency.MONTHLY, today.plusDays(3), admin, streaming, paypal);
        saveSubscriptionIfAbsent("YouTube Premium", new BigDecimal("13.99"), BillingFrequency.MONTHLY, today.plusDays(20), admin, streaming, visa);
        saveSubscriptionIfAbsent("Disney+", new BigDecimal("13.99"), BillingFrequency.MONTHLY, today.plusDays(25), admin, streaming, visa);
        saveSubscriptionIfAbsent("GitHub Copilot", new BigDecimal("10.00"), BillingFrequency.MONTHLY, today.plusDays(7), admin, software, visa);
        saveSubscriptionIfAbsent("JetBrains All Products", new BigDecimal("249.00"), BillingFrequency.YEARLY, today.plusDays(90), admin, software, visa);
        saveSubscriptionIfAbsent("Adobe Creative Cloud", new BigDecimal("54.99"), BillingFrequency.MONTHLY, today.plusDays(15), admin, software, mastercard);
        saveSubscriptionIfAbsent("Google One 2 TB", new BigDecimal("9.99"), BillingFrequency.MONTHLY, today.plusDays(5), admin, cloudStore, paypal);
        saveSubscriptionIfAbsent("iCloud+ 200 GB", new BigDecimal("2.99"), BillingFrequency.MONTHLY, today.plusDays(18), admin, cloudStore, visa);
        saveSubscriptionIfAbsent("Xbox Game Pass Ultimate", new BigDecimal("14.99"), BillingFrequency.MONTHLY, today.plusDays(9), admin, gaming, mastercard);
        saveSubscriptionIfAbsent("Headspace", new BigDecimal("69.99"), BillingFrequency.YEARLY, today.plusDays(60), admin, health, paypal);
        saveSubscriptionIfAbsent("The New York Times", new BigDecimal("17.00"), BillingFrequency.MONTHLY, today.plusDays(22), admin, news, visa);
        saveSubscriptionIfAbsent("Duolingo Plus", new BigDecimal("83.99"), BillingFrequency.YEARLY, today.plusDays(45), admin, education, paypal);
        saveSubscriptionIfAbsent("HelloFresh", new BigDecimal("79.99"), BillingFrequency.MONTHLY, today.plusDays(2), admin, food, mastercard);
    }

    private Category getOrCreateCategory(String name, String description, User user, Long userId) {
        if (categoryRepository.existsByNameAndUserId(name, userId)) {
            return categoryRepository.findByUserId(userId).stream()
                    .filter(c -> c.getName().equals(name))
                    .findFirst().orElseThrow();
        }
        return categoryRepository.save(new Category(name, description, user));
    }

    private PaymentMethod getOrCreatePaymentMethod(PaymentType type, String details, User user) {
        return paymentMethodRepository.findByUserId(user.getId()).stream()
                .filter(pm -> pm.getType() == type && pm.getDetails().equals(details))
                .findFirst()
                .orElseGet(() -> {
                    PaymentMethod pm = new PaymentMethod();
                    pm.setType(type);
                    pm.setDetails(details);
                    pm.setUser(user);
                    return paymentMethodRepository.save(pm);
                });
    }

    private void saveSubscriptionIfAbsent(String name, BigDecimal price, BillingFrequency frequency,
                                          LocalDate renewalDate, User user, Category category,
                                          PaymentMethod paymentMethod) {
        boolean exists = subscriptionRepository.findByUserId(user.getId()).stream()
                .anyMatch(s -> s.getName().equals(name));
        if (exists) return;
        Subscription s = new Subscription(name, price, frequency, renewalDate, user);
        s.setCategory(category);
        s.setPaymentMethod(paymentMethod);
        subscriptionRepository.save(s);
    }
}