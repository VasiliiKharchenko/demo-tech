package org.techspec.demotech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.techspec.demotech.dto.CreateSubscriptionRequest;
import org.techspec.demotech.dto.SubscriptionDto;
import org.techspec.demotech.exception.SubscriptionNotFoundException;
import org.techspec.demotech.exception.UserNotFoundException;
import org.techspec.demotech.service.SubscriptionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@ActiveProfiles("test")
@DisplayName("SubscriptionController Integration Tests")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @Autowired
    private ObjectMapper objectMapper;

    private SubscriptionDto testSubscriptionDto;
    private CreateSubscriptionRequest createSubscriptionRequest;

    @BeforeEach
    void setUp() {
        testSubscriptionDto = SubscriptionDto.builder()
                .id(1L)
                .serviceName("Netflix")
                .price(new BigDecimal("299.00"))
                .createdAt(LocalDateTime.now())
                .build();

        createSubscriptionRequest = new CreateSubscriptionRequest("Netflix", new BigDecimal("299.00"));
    }

    @Test
    @DisplayName("POST /users/{userId}/subscriptions должен создать подписку")
    void createSubscription_WhenValidRequest_ShouldReturn201() throws Exception {

        Long userId = 1L;
        when(subscriptionService.createSubscription(eq(userId), any(CreateSubscriptionRequest.class)))
                .thenReturn(testSubscriptionDto);

        mockMvc.perform(post("/users/{userId}/subscriptions", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubscriptionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceName").value("Netflix"))
                .andExpect(jsonPath("$.price").value(299.00));

        verify(subscriptionService).createSubscription(eq(userId), any(CreateSubscriptionRequest.class));
    }

    @Test
    @DisplayName("POST /users/{userId}/subscriptions должен вернуть 400 при невалидных данных")
    void createSubscription_WhenInvalidRequest_ShouldReturn400() throws Exception {

        Long userId = 1L;
        CreateSubscriptionRequest invalidRequest = new CreateSubscriptionRequest("", new BigDecimal("-1"));

        mockMvc.perform(post("/users/{userId}/subscriptions", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).createSubscription(any(), any());
    }

    @Test
    @DisplayName("POST /users/{userId}/subscriptions должен вернуть 404 когда пользователь не найден")
    void createSubscription_WhenUserNotFound_ShouldReturn404() throws Exception {

        Long userId = 999L;
        when(subscriptionService.createSubscription(eq(userId), any(CreateSubscriptionRequest.class)))
                .thenThrow(new UserNotFoundException("Пользователь с ID " + userId + " не найден"));

        mockMvc.perform(post("/users/{userId}/subscriptions", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSubscriptionRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users/{userId}/subscriptions должен вернуть подписки пользователя")
    void getUserSubscriptions_WhenUserExists_ShouldReturn200() throws Exception {

        Long userId = 1L;
        SubscriptionDto subscription2 = SubscriptionDto.builder()
                .id(2L)
                .serviceName("Spotify")
                .price(new BigDecimal("199.00"))
                .build();

        List<SubscriptionDto> subscriptions = Arrays.asList(testSubscriptionDto, subscription2);
        when(subscriptionService.getUserSubscriptions(userId)).thenReturn(subscriptions);

        mockMvc.perform(get("/users/{userId}/subscriptions", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].serviceName").value("Netflix"))
                .andExpect(jsonPath("$[1].serviceName").value("Spotify"));

        verify(subscriptionService).getUserSubscriptions(userId);
    }

    @Test
    @DisplayName("DELETE /users/{userId}/subscriptions/{subscriptionId} должен удалить подписку")
    void deleteSubscription_WhenSubscriptionExists_ShouldReturn204() throws Exception {

        Long userId = 1L;
        Long subscriptionId = 1L;
        doNothing().when(subscriptionService).deleteSubscription(userId, subscriptionId);

        mockMvc.perform(delete("/users/{userId}/subscriptions/{subscriptionId}", userId, subscriptionId))
                .andExpect(status().isNoContent());

        verify(subscriptionService).deleteSubscription(userId, subscriptionId);
    }

    @Test
    @DisplayName("DELETE /users/{userId}/subscriptions/{subscriptionId} должен вернуть 404 когда подписка не найдена")
    void deleteSubscription_WhenSubscriptionNotFound_ShouldReturn404() throws Exception {

        Long userId = 1L;
        Long subscriptionId = 999L;
        doThrow(new SubscriptionNotFoundException("Подписка не найдена"))
                .when(subscriptionService).deleteSubscription(userId, subscriptionId);

        mockMvc.perform(delete("/users/{userId}/subscriptions/{subscriptionId}", userId, subscriptionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /subscriptions/top должен вернуть топ-3 подписок")
    void getTopSubscriptions_ShouldReturn200() throws Exception {

        Map<String, Object> top1 = new HashMap<>();
        top1.put("serviceName", "Netflix");
        top1.put("subscribersCount", 100L);

        Map<String, Object> top2 = new HashMap<>();
        top2.put("serviceName", "Spotify");
        top2.put("subscribersCount", 80L);

        List<Map<String, Object>> topSubscriptions = Arrays.asList(top1, top2);
        when(subscriptionService.getTopSubscriptions()).thenReturn(topSubscriptions);

        mockMvc.perform(get("/subscriptions/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].serviceName").value("Netflix"))
                .andExpect(jsonPath("$[0].subscribersCount").value(100))
                .andExpect(jsonPath("$[1].serviceName").value("Spotify"));

        verify(subscriptionService).getTopSubscriptions();
    }
}
