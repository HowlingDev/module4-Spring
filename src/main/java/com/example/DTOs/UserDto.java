package com.example.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Сущность пользователя")
@Data
@AllArgsConstructor @NoArgsConstructor
public class UserDto {

    @Schema(description = "Идентификатор пользователя (должен быть null, иначе будет проигнорировано)")
    private Long id;

    @Schema(description = "Имя пользователя", example = "example_user_name")
    @NotBlank(message = "имя не должно быть пустым")
    private String name;

    @Schema(description = "email пользователя", example = "user@example.com")
    @Email(message = "Некорректный формат email.")
    private String email;

    @Schema(description = "Возраст пользователя")
    @Min(value = 14, message = "минимальный возраст - 14")
    private int age;

}
