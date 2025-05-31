package org.techspec.demotech.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.techspec.demotech.dto.CreateSubscriptionRequest;
import org.techspec.demotech.dto.SubscriptionDto;
import org.techspec.demotech.service.SubscriptionService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscriptions", description = "API для управления подписками")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/users/{userId}/subscriptions")
    @Operation(summary = "Добавить подписку", description = "Добавление подписки пользователю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Подписка успешно создана"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или подписка уже существует")
    })
    public ResponseEntity<SubscriptionDto> createSubscription(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("Запрос на создание подписки для пользователя ID: {}", userId);
        SubscriptionDto subscription = subscriptionService.createSubscription(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @GetMapping("/users/{userId}/subscriptions")
    @Operation(summary = "Получить подписки пользователя", description = "Получение списка подписок пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список подписок получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<List<SubscriptionDto>> getUserSubscriptions(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId) {
        log.info("Запрос подписок для пользователя ID: {}", userId);
        List<SubscriptionDto> subscriptions = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @DeleteMapping("/users/{userId}/subscriptions/{subscriptionId}")
    @Operation(summary = "Удалить подписку", description = "Удаление подписки пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Подписка успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Подписка или пользователь не найден")
    })
    public ResponseEntity<Void> deleteSubscription(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId,
            @Parameter(description = "ID подписки", required = true)
            @PathVariable Long subscriptionId) {
        log.info("Запрос на удаление подписки ID: {} для пользователя ID: {}", subscriptionId, userId);
        subscriptionService.deleteSubscription(userId, subscriptionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions/top")
    @Operation(summary = "Получить ТОП-3 популярных подписок",
            description = "Получение списка 3 самых популярных подписок по количеству подписчиков")
    @ApiResponse(responseCode = "200", description = "Список популярных подписок получен")
    public ResponseEntity<List<Map<String, Object>>> getTopSubscriptions() {
        log.info("Запрос топ-3 популярных подписок");
        List<Map<String, Object>> topSubscriptions = subscriptionService.getTopSubscriptions();
        return ResponseEntity.ok(topSubscriptions);
    }
}
