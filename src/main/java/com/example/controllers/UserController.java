package com.example.controllers;

import com.example.DTOs.UserDto;
import com.example.services.KafkaProducerService;
import com.example.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@Tag(name = "Пользователи", description = "Взаимодействие с пользователями")
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

    @Operation(
            summary = "Создание пользователя",
            description = "Позволяет создать нового пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Данные введены некорректно"),
            @ApiResponse(responseCode = "404", description = "Не используется для метода", content = @Content)}
    )
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
public EntityModel<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        kafkaProducerService.sendMessage("actions", "CREATE " + userDto.getEmail());
        return convertToHateoasEntityModel(userService.create(userDto));
    }

    @Operation(
            summary = "Получение пользователя по ID",
            description = "Позволяет найти пользователя по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "id должен быть не меньше 1"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)}
    )
    @GetMapping("/{id}")
    public EntityModel<UserDto> findUserById(@PathVariable @Min(1)
                                    @Parameter(description = "Идентификатор пользователя (должен быть не меньше 1)")
                                    Long id) {
        return convertToHateoasEntityModel(userService.findUserById(id));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException e) {
        return "Пользователь не найден";
    }

    @Operation(
            summary = "Получение списка всех пользователей",
            description = "Позволяет найти всех пользователей"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей получен",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "400", description = "Без параметров"),
            @ApiResponse(responseCode = "404", description = "Не используется для метода", content = @Content)}
    )
    @GetMapping("/all")
    public CollectionModel<EntityModel<UserDto>> findAllUsers() {
        List<EntityModel<UserDto>> userDtoList = userService.findAll().stream()
                .map(userDto -> EntityModel.of(userDto, linkTo(methodOn(UserController.class)
                        .findUserById(userDto.getId()))
                        .withSelfRel()))
                .toList();
        Link link = linkTo(methodOn(UserController.class).findAllUsers())
                .withRel("all-users");
        return CollectionModel.of(userDtoList, link);
    }

    @Operation(
            summary = "Обновление пользователя",
            description = "Позволяет обновить информацию о пользователе с заданным ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Обновление пользователя прошло успешно",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Данные введены некорректно"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)}
    )
    @PutMapping("/update/{id}")
    public EntityModel<UserDto> updateUser(@PathVariable @Min(1)
                                  @Parameter(description = "Идентификатор пользователя (должен быть не меньше 1)")
                                  Long id,
                              @Valid @RequestBody UserDto userDto) {
        return convertToHateoasEntityModel(userService.update(id, userDto));
    }

    @Operation(
            summary = "Удаление пользователя",
            description = "Позволяет удалить пользователя с заданным ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Удаление пользователя прошло успешно",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "id должен быть не меньше 1"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)}
    )
    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable @Min(1)
                               @Parameter(description = "Идентификатор пользователя (должен быть не меньше 1)")
                               Long id) {
        try {
            UserDto userDto = userService.findUserById(id);
            if (userDto != null) {
                kafkaProducerService.sendMessage("actions", "DELETE " + userDto.getEmail());
            }
        } catch (EntityNotFoundException e) {
            System.out.println("Пользователь не найден, поэтому сообщение не отправлено.");
        }
        userService.delete(id);
    }

    private EntityModel<UserDto> convertToHateoasEntityModel(UserDto userDto) {
        Link selfLink = linkTo(methodOn(UserController.class).findUserById(userDto.getId()))
                .withSelfRel();
        Link allUsersLink = linkTo(methodOn(UserController.class).findAllUsers())
                .withRel("all-users");
        return EntityModel.of(userDto, selfLink, allUsersLink);
    }

    @Operation(
            summary = "Информация об API",
            description = "Дает ссылки на получение одного пользователя или всех пользователей"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ссылки получены", content = @Content),
            @ApiResponse(responseCode = "400", description = "Не используется для метода", content = @Content),
            @ApiResponse(responseCode = "404", description = "Не используется для метода", content = @Content)}
    )
    @Tag(name = "Информация")
    @GetMapping
    public Map<String, String> apiInfo() {
        Map<String, String> methods = new HashMap<>();
        methods.put("find_user_by_id", Link.of("/users/{id}").getHref());
        methods.put("find_all_users", Link.of("/users/all").getHref());
        return methods;
    }
}

