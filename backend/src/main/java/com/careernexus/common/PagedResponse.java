package com.careernexus.common;

import java.util.List;

public record PagedResponse<T>(List<T> items, long total, int page, int size, int totalPages) {
    public static <T> PagedResponse<T> of(List<T> items, long total, int page, int size) {
        return new PagedResponse<>(items, total, page, size, (int) Math.ceil((double) total / size));
    }
}

