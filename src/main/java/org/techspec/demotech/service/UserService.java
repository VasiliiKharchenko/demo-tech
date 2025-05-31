package org.techspec.demotech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techspec.demotech.dto.CreateUserRequest;
import org.techspec.demotech.dto.UpdateUserRequest;
import org.techspec.demotech.dto.UserDto;
import org.techspec.demotech.entity.User;
import org.techspec.demotech.exception.UserNotFoundException;
import org.techspec.demotech.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        log.info("Создание пользователя с email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с ID: {}", savedUser.getId());

        return convertToDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        log.info("Поиск пользователя с ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));

        return convertToDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.info("Получение списка всех пользователей");

        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        log.info("Обновление пользователя с ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        log.info("Пользователь с ID {} обновлен", id);

        return convertToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }

        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удален", id);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
