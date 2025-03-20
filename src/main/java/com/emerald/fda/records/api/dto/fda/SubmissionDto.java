package com.emerald.fda.records.api.dto.fda;

/**
 * Data Transfer Object for a submission in a drug application.
 */
public record SubmissionDto(
        String submission_type,
        String submission_number,
        String submission_status,
        String submission_status_date,
        String submission_class_code,
        String submission_class_code_description
) {}