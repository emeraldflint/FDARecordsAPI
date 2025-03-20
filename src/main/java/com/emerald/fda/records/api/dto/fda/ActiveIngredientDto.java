package com.emerald.fda.records.api.dto.fda;


/**
 * Data Transfer Object for an active ingredient in a product.
 */
public record ActiveIngredientDto(
        String name,
        String strength
) {}