package com.emerald.fda.records.api.dto.fda;

import java.util.List;

/**
 * Data Transfer Object for a product in a drug application.
 */
public record ProductDto(
        String product_number,
        String reference_drug,
        String brand_name,
        List<ActiveIngredientDto> active_ingredients,
        String reference_standard,
        String dosage_form,
        String route,
        String marketing_status
) {}