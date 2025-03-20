package com.emerald.fda.records.api.dto.fda;

/**
 * Data Transfer Object for the results metadata section of the FDA API response.
 */
public record ResultsMetaDto(
        int skip,
        int limit,
        int total
) {}