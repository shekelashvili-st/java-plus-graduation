package ru.yandex.practicum.core.event.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.yandex.practicum.core.event.compilation.entity.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long>, JpaSpecificationExecutor<Compilation> {

}
