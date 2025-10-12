package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor @NoArgsConstructor
public class UserDto {
    private Long id;

    private String name;

    private String email;

    private int age;

    private LocalDate created_at;
}
