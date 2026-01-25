package ru.yandex.practicum.core.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.core.common.dto.user.NewUserRequest;
import ru.yandex.practicum.core.common.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);

    UserDto getUserById(Long userId);

    Page<UserDto> getUsersPage(List<Long> ids, Pageable pageable);

    List<UserDto> getUsersById(Iterable<Long> userIds);
}