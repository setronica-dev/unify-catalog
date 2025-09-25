package com.setronica.unify.application.service;

import com.setronica.unify.application.dto.ValidationResultDto;

/**
 * This business service exposes basic validation schema operations.
 */
public interface SchemaWebService {
    /**
     * Retrieve the current schema for the product category.
     *
     * @param category the key
     * @return the plain text schema representation
     */
    String getSchema(String category);

    /**
     * Update or create the validation schema for the specified product category.
     *
     * @param category the key
     * @param content  the plain text schema representation
     * @return the outcome of the operation, including validation errors if unsuccessful
     */
    ValidationResultDto setSchema(String category, String content);
}
