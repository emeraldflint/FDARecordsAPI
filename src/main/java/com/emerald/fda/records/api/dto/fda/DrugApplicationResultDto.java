package com.emerald.fda.records.api.dto.fda;

import java.util.List;

/**
 * Data Transfer Object for a drug application result from the FDA API.
 */
public record DrugApplicationResultDto(
        List<SubmissionDto> submissions,
        String application_number,
        String sponsor_name,
        OpenFdaDto openfda,
        List<ProductDto> products
) {}