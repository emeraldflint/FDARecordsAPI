package com.emerald.fda.records.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * Data Transfer Object for manually storing a drug application record with specific required fields.
 */
public record StoreDrugApplicationRecordDto(
        @NotBlank(message = "Application number is required")
        String applicationNumber,

        @NotBlank(message = "Manufacturer name is required")
        String manufacturerName,

        @NotBlank(message = "Substance name is required")
        String substanceName,

        @NotEmpty(message = "At least one product number is required")
        Set<String> productNumbers
) {}