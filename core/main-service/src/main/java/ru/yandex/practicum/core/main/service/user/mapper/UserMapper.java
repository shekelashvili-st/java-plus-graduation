package ru.yandex.practicum.core.main.service.user.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.core.main.service.user.dto.NewUserRequest;
import ru.yandex.practicum.core.main.service.user.dto.UserDto;
import ru.yandex.practicum.core.main.service.user.dto.UserShortDto;
import ru.yandex.practicum.core.main.service.user.entity.User;

@UtilityClass
public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public static UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }

    public static User toUser(NewUserRequest newUserRequest) {
        User user = new User();
        user.setEmail(newUserRequest.getEmail());
        user.setName(newUserRequest.getName());
        return user;
    }

    public static User toUser(UserDto userDto) {
        return userDto == null ? null :
                new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }
}
