package com.example;

import com.example.DTOs.UserDto;
import com.example.controllers.UserController;
import com.example.services.KafkaProducerService;
import com.example.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    KafkaProducerService kafkaProducerService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Успешное создание пользователя")
    public void createUserTest() throws Exception {
        UserDto userDto = new UserDto(1L, "Vasya", "vasya@gmail.com", 20);
        when(userService.create(any(UserDto.class))).thenReturn(userDto);
        doNothing().when(kafkaProducerService).sendMessage(anyString(),anyString());

        mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Vasya"))
                .andExpect(jsonPath("$.email").value("vasya@gmail.com"))
                .andExpect(jsonPath("$.age").value(20));
    }

    @Test
    @DisplayName("Пользователь найден")
    public void findUserByIdTest_userFound() throws Exception{
        UserDto userDto = new UserDto(1L, "Vasya", "vasya@gmail.com", 20);
        when(userService.findUserById(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Vasya"))
                .andExpect(jsonPath("$.email").value("vasya@gmail.com"))
                .andExpect(jsonPath("$.age").value(20));
    }

    @Test
    @DisplayName("Пользователь не найден")
    public void findUserByIdTest_userNotFound() throws Exception {
        when(userService.findUserById(anyLong())).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Все пользователи найдены")
    public void findAllUsersTest_returnUsers() throws Exception {
        List<UserDto> userDtoList = new ArrayList<>();
        UserDto userDto1 = new UserDto(1L, "Vasya", "vasya@gmail.com", 20);
        UserDto userDto2 = new UserDto(2L, "Dima", "dima@gmail.com", 25);
        UserDto userDto3 = new UserDto(3L, "Vlad", "vlad@gmail.com", 21);
        userDtoList.add(userDto1);
        userDtoList.add(userDto2);
        userDtoList.add(userDto3);
        when(userService.findAll()).thenReturn(userDtoList);

        mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userDtoList", hasSize(3)));
    }

    @Test
    @DisplayName("Пустой список: пользователей нет")
    public void findAllUsersTest_returnEmptyList() throws Exception {
        when(userService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }

    @Test
    @DisplayName("Успешное обновление пользователя")
    public void updateUserTest_successfulUpdate() throws Exception {
        UserDto userDto = new UserDto(1L, "Vasya update", "vasya@gmail.com", 20);
        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(put("/users/update/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Vasya update"))
                .andExpect(jsonPath("$.email").value("vasya@gmail.com"))
                .andExpect(jsonPath("$.age").value(20));
    }

    @Test
    @DisplayName("Не удалось обновить пользователя, потому что он не найден")
    public void updateUserTest_updateFailedBecauseOfEntityNotFoundException() throws Exception {
        UserDto userDto = new UserDto(1L, "Vasya update", "vasya@gmail.com", 20);
        when(userService.update(anyLong(), any(UserDto.class))).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(put("/users/update/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Не удалось обновить пользователя из-за плохого запроса")
    public void updateUserTest_updateFailedBecauseOfBadRequest() throws Exception {
        when(userService.update(anyLong(), any(UserDto.class))).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(put("/users/update/{id}", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Пользователь удален")
    public void deleteUserTest() throws Exception {
        doNothing().when(userService).delete(anyLong());
        doNothing().when(kafkaProducerService).sendMessage(anyString(),anyString());

        mockMvc.perform(delete("/users/delete/{id}", 1L))
                .andExpect(status().isOk());
    }
}
