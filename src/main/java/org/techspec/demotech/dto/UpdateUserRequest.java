package org.techspec.demotech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 255, message = "Имя должно содержать от 2 до 255 символов")
    private String name;

    @Email(message = "Некорректный формат email")
    private String email;
}
