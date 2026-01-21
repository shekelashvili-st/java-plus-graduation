package ru.yandex.practicum.core.event.compilation.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.core.event.compilation.entity.Compilation;

public class CompilationSpecs {

    public static Specification<Compilation> isPinned(boolean value) {
        return (root, query, builder) ->
                builder.equal(root.get("pinned"), value);
    }
}
