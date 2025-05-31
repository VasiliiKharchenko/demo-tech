package org.techspec.demotech;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.techspec.demotech.entity.User;
import org.techspec.demotech.entity.Subscription;
import org.techspec.demotech.repository.UserRepository;
import org.techspec.demotech.repository.SubscriptionRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DemoTechApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    void contextLoads() {

        assertThat(userRepository).isNotNull();
        assertThat(subscriptionRepository).isNotNull();
    }

    @Test
    void h2DatabaseConnectionTest() {

        User testUser = User.builder()
                .name("H2 Тест Пользователь")
                .email("h2test@example.com")
                .build();

        User savedUser = userRepository.save(testUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("H2 Тест Пользователь");
        assertThat(savedUser.getEmail()).isEqualTo("h2test@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("H2 Тест Пользователь");

        Optional<User> foundByEmail = userRepository.findByEmail("h2test@example.com");
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void h2DatabaseWithSubscriptionTest() {

        User user = User.builder()
                .name("Пользователь с подписками")
                .email("subscriptions@example.com")
                .build();
        User savedUser = userRepository.save(user);

        Subscription subscription = Subscription.builder()
                .user(savedUser)
                .serviceName("Netflix H2 Test")
                .price(new BigDecimal("299.99"))
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        assertThat(savedSubscription.getId()).isNotNull();
        assertThat(savedSubscription.getServiceName()).isEqualTo("Netflix H2 Test");
        assertThat(savedSubscription.getPrice()).isEqualByComparingTo(new BigDecimal("299.99"));
        assertThat(savedSubscription.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(savedSubscription.getCreatedAt()).isNotNull();

        var userSubscriptions = subscriptionRepository.findByUserId(savedUser.getId());
        assertThat(userSubscriptions).hasSize(1);
        assertThat(userSubscriptions.get(0).getServiceName()).isEqualTo("Netflix H2 Test");

        boolean exists = subscriptionRepository.existsByUserIdAndServiceName(
                savedUser.getId(), "Netflix H2 Test");
        assertThat(exists).isTrue();
    }

    @Test
    void h2DatabaseUniqueConstraintTest() {

        User user1 = User.builder()
                .name("пользователь")
                .email("unique@example.com")
                .build();
        userRepository.save(user1);

        boolean emailExists = userRepository.existsByEmail("unique@example.com");
        assertThat(emailExists).isTrue();

        boolean nonExistentEmail = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(nonExistentEmail).isFalse();
    }

    @Test
    void h2DatabaseQueryTest() {

        User user1 = userRepository.save(User.builder()
                .name("Пользователь 1")
                .email("user1@example.com")
                .build());

        User user2 = userRepository.save(User.builder()
                .name("Пользователь 2")
                .email("user2@example.com")
                .build());

        subscriptionRepository.save(Subscription.builder()
                .user(user1)
                .serviceName("Netflix")
                .price(new BigDecimal("299.00"))
                .build());

        subscriptionRepository.save(Subscription.builder()
                .user(user2)
                .serviceName("Netflix")
                .price(new BigDecimal("299.00"))
                .build());

        subscriptionRepository.save(Subscription.builder()
                .user(user1)
                .serviceName("Spotify")
                .price(new BigDecimal("199.00"))
                .build());

        var topServices = subscriptionRepository.findTopServicesBySubscriptionCount();
        assertThat(topServices).isNotEmpty();

        assertThat(topServices.get(0)[0]).isEqualTo("Netflix");
        assertThat(topServices.get(0)[1]).isEqualTo(2L);
    }

    @Test
    void h2DatabaseTransactionTest() {

        long initialCount = userRepository.count();

        User user = User.builder()
                .name("Транзакционный пользователь")
                .email("transaction@example.com")
                .build();

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();

        long currentCount = userRepository.count();
        assertThat(currentCount).isEqualTo(initialCount + 1);

    }
}
