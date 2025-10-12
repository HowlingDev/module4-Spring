package com.example.DTOs;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;

    private String name;

    private String email;

    private int age;

    private LocalDate created_at;
}
