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
import org.techspec.demotech.dto.CreateUserRequest;
import org.techspec.demotech.dto.UpdateUserRequest;
import org.techspec.demotech.dto.UserDto;
import org.techspec.demotech.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Создать пользователя", description = "Создание нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Запрос на создание пользователя: {}", request.getEmail());
        UserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя", description = "Получение информации о пользователе по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {
        log.info("Запрос информации о пользователе с ID: {}", id);
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей", description = "Получение списка всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей получен")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя", description = "Обновление данных пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Запрос на обновление пользователя с ID: {}", id);
        UserDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаление пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {
        log.info("Запрос на удаление пользователя с ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
