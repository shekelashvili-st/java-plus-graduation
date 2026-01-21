package ru.yandex.practicum.core.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.common.dto.user.UserDto;
import ru.yandex.practicum.core.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserDto> getUsersById(@RequestBody Iterable<Long> ids) {
        return userService.getUsersById(ids);
    }
}
