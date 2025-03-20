package com.emerald.fda.records.api.dto.fda;

import java.util.List;

/**
 * Data Transfer Object for the OpenFDA section of a drug application.
 */
public record OpenFdaDto(
        List<String> application_number,
        List<String> brand_name,
        List<String> generic_name,
        List<String> manufacturer_name,
        List<String> product_ndc,
        List<String> product_type,
        List<String> route,
        List<String> substance_name,
        List<String> rxcui,
        List<String> spl_id,
        List<String> spl_set_id,
        List<String> package_ndc,
        List<String> unii
) {}