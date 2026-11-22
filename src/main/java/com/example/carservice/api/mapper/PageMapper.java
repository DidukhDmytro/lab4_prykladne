package com.example.carservice.api.mapper;

import com.example.carservice.api.dto.PageResponse;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {
    public <S, T> PageResponse<T> toResponse(Page<S> page, Function<S, T> mapper) {
        List<T> content = page.getContent().stream()
                .map(mapper)
                .toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
