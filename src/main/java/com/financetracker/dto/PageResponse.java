package com.financetracker.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * The API's pagination envelope. We deliberately do NOT serialize Spring's
 * Page/PageImpl directly: its JSON shape is an internal detail of Spring Data
 * (it even logs a warning telling you not to), and it changes between versions.
 * This record is OUR contract — stable no matter what Spring does.
 *
 * @param content       the rows for this page
 * @param page          zero-based page index that was requested
 * @param size          page size that was applied (after clamping)
 * @param totalElements total matching rows across all pages
 * @param totalPages    total page count at this size
 * @param hasNext       true when another page exists — clients use this to
 *                      render a "next" control without computing it themselves
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }
}
