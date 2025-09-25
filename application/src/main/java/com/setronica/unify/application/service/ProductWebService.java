package com.setronica.unify.application.service;

import com.setronica.unify.application.dto.ProductDto;
import com.setronica.unify.application.dto.ValidationResultDto;

/**
 * This business service exposes basic product operations.
 */
public interface ProductWebService {
    /**
     * Get a single product.
     *
     * @param identifier the product's natural key
     * @return the product representation or null if the key does not exist
     */
    ProductDto getProduct(String identifier);

    /**
     * Create or update a product. All fields are required.
     *
     * @param identifier product's natural key
     * @param content    product's category and content
     * @return the outcome of the operation, including validation errors if unsuccessful
     */
    ValidationResultDto setProduct(String identifier, ProductDto content);
}
