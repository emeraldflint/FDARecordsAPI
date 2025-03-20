package com.emerald.fda.records.api.dto.fda;

/**
 * Data Transfer Object for the metadata section of the FDA API response.
 */
public record MetaDto(
        String disclaimer,
        String terms,
        String license,
        String last_updated,
        ResultsMetaDto results
) {}