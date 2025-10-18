package com.example.controllers;

import com.example.DTOs.UserDto;
import com.example.services.KafkaProducerService;
import com.example.services.UserService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final KafkaProducerService kafkaProducerService;

    private final UserService userService;

    @Autowired
    public UserController(UserService userService, KafkaProducerService kafkaProducerService) {
        this.userService = userService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("/create")
    public UserDto createUser(@RequestBody UserDto userDto) {
        kafkaProducerService.sendMessage("actions", "CREATE " + userDto.getEmail());
        return userService.create(userDto);
    }

    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleEntityNotFound(EntityNotFoundException e) {
        return "Пользователь не найден";
    }

    @GetMapping("/all")
    public List<UserDto> findAllUsers() {
        return userService.findAll();
    }

    @PutMapping("/update/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return userService.update(id, userDto);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable Long id) {
        try {
            kafkaProducerService.sendMessage("actions", "DELETE " + userService.findUserById(id).getEmail());
        } catch (EntityNotFoundException e) {
            System.out.println("Пользователь не найден, поэтому сообщение не отправлено.");
        }
        userService.delete(id);
    }
}
