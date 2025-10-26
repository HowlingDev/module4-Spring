package com.example.services;

import com.example.DTOs.UserDto;
import com.example.models.User;
import com.example.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    private List<UserDto> convertToDtoList (List<User> users) {
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : users) {
            usersDto.add(convertToDto(user));
        }
        return usersDto;
    }

    @Transactional
    public UserDto create(UserDto userDto) {
        User newUser = new User();
        BeanUtils.copyProperties(userDto, newUser, "id");
        return convertToDto(userRepository.save(newUser));
    }

    @Transactional(readOnly = true)
    public UserDto findUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь с ID = " + id + " не найден"));
        return convertToDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return convertToDtoList(userRepository.findAll());
    }

    @Transactional
    public UserDto update(Long id, UserDto updatedUser) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь с ID = " + id + " не найден"));
        BeanUtils.copyProperties(updatedUser, user, "id");
        return convertToDto(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
