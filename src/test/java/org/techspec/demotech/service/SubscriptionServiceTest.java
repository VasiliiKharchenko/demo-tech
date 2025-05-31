package org.techspec.demotech.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.techspec.demotech.dto.CreateSubscriptionRequest;
import org.techspec.demotech.dto.SubscriptionDto;
import org.techspec.demotech.entity.Subscription;
import org.techspec.demotech.entity.User;
import org.techspec.demotech.exception.SubscriptionNotFoundException;
import org.techspec.demotech.exception.UserNotFoundException;
import org.techspec.demotech.repository.SubscriptionRepository;
import org.techspec.demotech.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService Unit Tests")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User testUser;
    private Subscription testSubscription;
    private CreateSubscriptionRequest createSubscriptionRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Тест Пользователь")
                .email("test@example.com")
                .build();

        testSubscription = Subscription.builder()
                .id(1L)
                .user(testUser)
                .serviceName("Netflix")
                .price(new BigDecimal("299.00"))
                .createdAt(LocalDateTime.now())
                .build();

        createSubscriptionRequest = new CreateSubscriptionRequest("Netflix", new BigDecimal("299.00"));
    }

    @Test
    @DisplayName("Должен успешно создать подписку")
    void createSubscription_WhenValidRequest_ShouldReturnSubscriptionDto() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.existsByUserIdAndServiceName(userId, "Netflix")).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        SubscriptionDto result = subscriptionService.createSubscription(userId, createSubscriptionRequest);

        assertThat(result).isNotNull();
        assertThat(result.getServiceName()).isEqualTo("Netflix");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("299.00"));

        verify(userRepository).findById(userId);
        verify(subscriptionRepository).existsByUserIdAndServiceName(userId, "Netflix");
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение когда пользователь не найден")
    void createSubscription_WhenUserNotExists_ShouldThrowException() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.createSubscription(userId, createSubscriptionRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при создании дублирующей подписки")
    void createSubscription_WhenSubscriptionExists_ShouldThrowException() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.existsByUserIdAndServiceName(userId, "Netflix")).thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.createSubscription(userId, createSubscriptionRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Подписка на сервис Netflix уже существует");

        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Должен вернуть подписки пользователя")
    void getUserSubscriptions_WhenUserExists_ShouldReturnSubscriptions() {

        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Arrays.asList(testSubscription));

        List<SubscriptionDto> result = subscriptionService.getUserSubscriptions(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServiceName()).isEqualTo("Netflix");

        verify(userRepository).existsById(userId);
        verify(subscriptionRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Должен успешно удалить подписку")
    void deleteSubscription_WhenSubscriptionExists_ShouldDeleteSubscription() {

        Long userId = 1L;
        Long subscriptionId = 1L;
        when(subscriptionRepository.findByIdAndUserId(subscriptionId, userId))
                .thenReturn(Optional.of(testSubscription));

        subscriptionService.deleteSubscription(userId, subscriptionId);

        verify(subscriptionRepository).findByIdAndUserId(subscriptionId, userId);
        verify(subscriptionRepository).delete(testSubscription);
    }

    @Test
    @DisplayName("Должен выбросить исключение при удалении несуществующей подписки")
    void deleteSubscription_WhenSubscriptionNotExists_ShouldThrowException() {

        Long userId = 1L;
        Long subscriptionId = 1L;
        when(subscriptionRepository.findByIdAndUserId(subscriptionId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.deleteSubscription(userId, subscriptionId))
                .isInstanceOf(SubscriptionNotFoundException.class);

        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    @Test
    @DisplayName("Должен вернуть топ-3 популярных подписок")
    void getTopSubscriptions_ShouldReturnTopThree() {

        Object[] result1 = {"Netflix", 10L};
        Object[] result2 = {"Spotify", 8L};
        Object[] result3 = {"YouTube Premium", 5L};
        Object[] result4 = {"Amazon Prime", 3L};

        when(subscriptionRepository.findTopServicesBySubscriptionCount())
                .thenReturn(Arrays.asList(result1, result2, result3, result4));

        List<Map<String, Object>> result = subscriptionService.getTopSubscriptions();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).get("serviceName")).isEqualTo("Netflix");
        assertThat(result.get(0).get("subscribersCount")).isEqualTo(10L);

        verify(subscriptionRepository).findTopServicesBySubscriptionCount();
    }
}
