package org.techspec.demotech.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {

    @NotBlank(message = "Название сервиса обязательно для заполнения")
    @Size(min = 2, max = 255, message = "Название сервиса должно содержать от 2 до 255 символов")
    private String serviceName;

    @NotNull(message = "Цена обязательна для заполнения")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    private BigDecimal price;
}
