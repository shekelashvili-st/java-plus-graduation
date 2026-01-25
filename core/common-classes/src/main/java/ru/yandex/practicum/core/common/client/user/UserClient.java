package ru.yandex.practicum.core.common.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.core.common.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable Long id);

    @GetMapping
    List<UserDto> getUsersById(@RequestBody Iterable<Long> ids);
}
