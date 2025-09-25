package com.setronica.unify.catalog.service;

import com.setronica.unify.catalog.domain.Product;

/**
 * This service handles the product persistent entity.
 */
public interface ProductService {
    /**
     * Retrieve an existing product.
     *
     * @param identifier product's natural key
     * @return the requested product, or null if it does not exist
     */
    Product getProduct(String identifier);

    /**
     * Create or update a product. Provided domain entity must have all of: identifier, content, category, and the
     * validation schema object provided by the content validation service. The product should be valid.
     *
     * @param product the product to save
     * @throws IllegalArgumentException if any of the required fields are empty
     */
    void saveProduct(Product product);
}
