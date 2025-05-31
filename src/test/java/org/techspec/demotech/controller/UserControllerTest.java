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
import org.techspec.demotech.dto.CreateUserRequest;
import org.techspec.demotech.dto.UpdateUserRequest;
import org.techspec.demotech.dto.UserDto;
import org.techspec.demotech.exception.UserNotFoundException;
import org.techspec.demotech.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto testUserDto;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .id(1L)
                .name("Тест Пользователь")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createUserRequest = new CreateUserRequest("Тест Пользователь", "test@example.com");
    }

    @Test
    @DisplayName("POST /users должен создать пользователя")
    void createUser_WhenValidRequest_ShouldReturn201() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Тест Пользователь"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /users должен вернуть 400 при невалидных данных")
    void createUser_WhenInvalidRequest_ShouldReturn400() throws Exception {

        CreateUserRequest invalidRequest = new CreateUserRequest("", "invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("GET /users/{id} должен вернуть пользователя")
    void getUserById_WhenUserExists_ShouldReturn200() throws Exception {

        Long userId = 1L;
        when(userService.getUserById(userId)).thenReturn(testUserDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Тест Пользователь"));

        verify(userService).getUserById(userId);
    }

    @Test
    @DisplayName("GET /users/{id} должен вернуть 404 когда пользователь не найден")
    void getUserById_WhenUserNotExists_ShouldReturn404() throws Exception {

        Long userId = 1L;
        when(userService.getUserById(userId))
                .thenThrow(new UserNotFoundException("Пользователь с ID " + userId + " не найден"));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(userId);
    }

    @Test
    @DisplayName("GET /users должен вернуть всех пользователей")
    void getAllUsers_ShouldReturn200() throws Exception {

        UserDto user2 = UserDto.builder()
                .id(2L)
                .name("Второй Пользователь")
                .email("second@example.com")
                .build();

        List<UserDto> users = Arrays.asList(testUserDto, user2);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Тест Пользователь"))
                .andExpect(jsonPath("$[1].name").value("Второй Пользователь"));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("PUT /users/{id} должен обновить пользователя")
    void updateUser_WhenValidRequest_ShouldReturn200() throws Exception {

        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest("Обновленное Имя", null);
        UserDto updatedUser = UserDto.builder()
                .id(userId)
                .name("Обновленное Имя")
                .email("test@example.com")
                .build();

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленное Имя"));

        verify(userService).updateUser(eq(userId), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("DELETE /users/{id} должен удалить пользователя")
    void deleteUser_WhenUserExists_ShouldReturn204() throws Exception {

        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }
}
