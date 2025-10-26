package com.example.controllers.ValidationResponse;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Hidden
@Getter
@RequiredArgsConstructor
public class Violation {

    private final String fieldName;

    private final String message;
}
