package com.emerald.fda.records.api.dto.fda;

import java.util.List;

/**
 * Data Transfer Object for the root FDA API response.
 */
public record FdaResponseDto(
        MetaDto meta,
        List<DrugApplicationResultDto> results
) {}