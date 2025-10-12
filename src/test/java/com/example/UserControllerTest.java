package com.example;

import com.example.DTOs.UserDto;
import com.example.controllers.UserController;
import com.example.services.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    public void createUserTest() throws Exception {
        UserDto userDto = new UserDto(1L,"Vasya", "vasya@gmail.com", 20, LocalDate.of(2025,10,12));
        when(userService.create(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Vasya"))
                .andExpect(jsonPath("$.email").value("vasya@gmail.com"))
                .andExpect(jsonPath("$.age").value(20))
                .andExpect(jsonPath("$.created_at").value(LocalDate.of(2025,10,12).toString()));
    }

    @Test
    public void findUserByIdTest_userFound() throws Exception{
        UserDto userDto = new UserDto(1L,"Vasya", "vasya@gmail.com", 20, LocalDate.of(2025,10,12));
        when(userService.findUserById(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Vasya"))
                .andExpect(jsonPath("$.email").value("vasya@gmail.com"))
                .andExpect(jsonPath("$.age").value(20))
                .andExpect(jsonPath("$.created_at").value(LocalDate.of(2025,10,12).toString()));
    }

    @Test
    public void findUserByIdTest_userNotFound() throws Exception {
        when(userService.findUserById(anyLong())).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void findAllUsersTest_returnUsers() throws Exception {
        List<UserDto> userDtoList = new ArrayList<>();
        UserDto userDto1 = new UserDto(1L,"Vasya", "vasya@gmail.com", 20, LocalDate.of(2025,10,12));
        UserDto userDto2 = new UserDto(2L,"Dima", "dima@gmail.com", 25, LocalDate.of(2025,10,12));
        UserDto userDto3 = new UserDto(3L,"Vlad", "vlad@gmail.com", 21, LocalDate.of(2025,10,12));
        userDtoList.add(userDto1);
        userDtoList.add(userDto2);
        userDtoList.add(userDto3);
        when(userService.findAll()).thenReturn(userDtoList);

        mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }

    @Test
    public void findAllUsersTest_returnEmptyList() throws Exception {
        when(userService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }

    @Test
    public void updateUserTest_successfulUpdate() throws Exception {
        UserDto userDto = new UserDto(1L,"Vasya update", "vasya@gmail.com", 20, LocalDate.of(2025,10,12));
        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(put("/users/{id}", userDto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Vasya update"))
                .andExpect(jsonPath("$.email").value("vasya@gmail.com"))
                .andExpect(jsonPath("$.age").value(20))
                .andExpect(jsonPath("$.created_at").value(LocalDate.of(2025,10,12).toString()));
    }

    @Test
    public void updateUserTest_updateFailedBecauseOfEntityNotFoundException() throws Exception {
        UserDto userDto = new UserDto(1L,"Vasya update", "vasya@gmail.com", 20, LocalDate.of(2025,10,12));
        when(userService.update(anyLong(), any(UserDto.class))).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(put("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void updateUserTest_updateFailedBecauseOfBadRequest() throws Exception {
        when(userService.update(anyLong(), any(UserDto.class))).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(put("/users/{id}", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteUserTest() throws Exception {
        doNothing().when(userService).delete(anyLong());

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());
    }
}
