package com.emerald.fda.records.api.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Generic Data Transfer Object for paginated responses.
 *
 * @param <T> the type of items in the page
 */
public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    /**
     * Creates a new PageResponseDto from a Spring Data Page.
     *
     * @param <U> the type of items in the page
     * @param page the Spring Data Page
     * @return a new PageResponseDto instance
     */
    public static <U> PageResponseDto<U> from(Page<U> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
