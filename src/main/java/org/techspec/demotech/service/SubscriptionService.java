package org.techspec.demotech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techspec.demotech.dto.CreateSubscriptionRequest;
import org.techspec.demotech.dto.SubscriptionDto;
import org.techspec.demotech.entity.Subscription;
import org.techspec.demotech.entity.User;
import org.techspec.demotech.exception.SubscriptionNotFoundException;
import org.techspec.demotech.exception.UserNotFoundException;
import org.techspec.demotech.repository.SubscriptionRepository;
import org.techspec.demotech.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public SubscriptionDto createSubscription(Long userId, CreateSubscriptionRequest request) {
        log.info("Создание подписки для пользователя ID: {}, сервис: {}", userId, request.getServiceName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));

        if (subscriptionRepository.existsByUserIdAndServiceName(userId, request.getServiceName())) {
            throw new IllegalArgumentException("Подписка на сервис " + request.getServiceName() + " уже существует");
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .serviceName(request.getServiceName())
                .price(request.getPrice())
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Подписка создана с ID: {}", savedSubscription.getId());

        return convertToDto(savedSubscription);
    }

    public List<SubscriptionDto> getUserSubscriptions(Long userId) {
        log.info("Получение подписок для пользователя ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSubscription(Long userId, Long subscriptionId) {
        log.info("Удаление подписки ID: {} для пользователя ID: {}", subscriptionId, userId);

        Subscription subscription = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "Подписка с ID " + subscriptionId + " не найдена для пользователя с ID " + userId));

        subscriptionRepository.delete(subscription);
        log.info("Подписка с ID {} удалена", subscriptionId);
    }

    public List<Map<String, Object>> getTopSubscriptions() {
        log.info("Получение топ-3 популярных подписок");

        List<Object[]> results = subscriptionRepository.findTopServicesBySubscriptionCount();

        return results.stream()
                .limit(3)
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("serviceName", result[0]);
                    item.put("subscribersCount", result[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    private SubscriptionDto convertToDto(Subscription subscription) {
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .serviceName(subscription.getServiceName())
                .price(subscription.getPrice())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
