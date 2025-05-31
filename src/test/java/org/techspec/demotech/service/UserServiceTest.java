package org.techspec.demotech.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.techspec.demotech.dto.CreateUserRequest;
import org.techspec.demotech.dto.UpdateUserRequest;
import org.techspec.demotech.dto.UserDto;
import org.techspec.demotech.entity.User;
import org.techspec.demotech.exception.UserNotFoundException;
import org.techspec.demotech.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Тест Пользователь")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createUserRequest = new CreateUserRequest("Тест Пользователь", "test@example.com");
        updateUserRequest = new UpdateUserRequest("Обновленное Имя", "updated@example.com");
    }

    @Test
    @DisplayName("Должен успешно создать пользователя")
    void createUser_WhenValidRequest_ShouldReturnUserDto() {

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.createUser(createUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testUser.getName());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при создании пользователя с существующим email")
    void createUser_WhenEmailExists_ShouldThrowException() {

        when(userRepository.existsByEmail(anyString())).thenReturn(true);


        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь с таким email уже существует");

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен успешно найти пользователя по ID")
    void getUserById_WhenUserExists_ShouldReturnUserDto() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserDto result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getName()).isEqualTo(testUser.getName());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Должен выбросить исключение когда пользователь не найден")
    void getUserById_WhenUserNotExists_ShouldThrowException() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + userId + " не найден");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Должен вернуть всех пользователей")
    void getAllUsers_ShouldReturnAllUsers() {

        User user2 = User.builder()
                .id(2L)
                .name("Второй Пользователь")
                .email("second@example.com")
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo(testUser.getName());
        assertThat(result.get(1).getName()).isEqualTo(user2.getName());

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Должен успешно обновить пользователя")
    void updateUser_WhenValidRequest_ShouldReturnUpdatedUserDto() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateUserRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.updateUser(userId, updateUserRequest);

        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении несуществующего пользователя")
    void updateUser_WhenUserNotExists_ShouldThrowException() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, updateUserRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен успешно удалить пользователя")
    void deleteUser_WhenUserExists_ShouldDeleteUser() {

        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Должен выбросить исключение при удалении несуществующего пользователя")
    void deleteUser_WhenUserNotExists_ShouldThrowException() {

        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }
}
