package com.example.controllers.ValidationResponse;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Hidden
@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {
    private final List<Violation> violations;
}
