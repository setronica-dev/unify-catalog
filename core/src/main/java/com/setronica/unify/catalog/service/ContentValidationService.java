package com.setronica.unify.catalog.service;

import com.setronica.unify.catalog.exception.ContentValidationException;
import com.setronica.unify.catalog.exception.SchemaValidationException;
import com.setronica.unify.persistence.entity.SchemaDefinition;

/**
 * This service is responsible for configuration and application of content validation schemas. Implementations are
 * free to use any content and schema types, standard (like JSON and JSON schema or XML and XSD) or anything custom.
 */
public interface ContentValidationService {
    /**
     * Get the current validation schema for product content.
     *
     * @param category product category
     * @return the validation schema in text form
     */
    String getSchema(String category);

    /**
     * Set a new product content validation schema for the specified product category.
     *
     * @param category product category
     * @param schema   the validation schema in text form
     * @throws SchemaValidationException if the schema is not valid according to its own rules or meta-schema
     */
    void setSchema(String category, String schema) throws SchemaValidationException;

    /**
     * Validate provided product content according to the latest schema configured for the specified product category.
     *
     * @param category product category
     * @param content  product content to be validated
     * @return the schema definition entity that was used for validation
     * @throws ContentValidationException if the specified category has no validation schema, if the content is not
     *                                    well-formed and cannot be validated, or if the content is not valid
     */
    SchemaDefinition validate(String category, String content) throws ContentValidationException;

    /**
     * Validate provided product content according to the latest schema configured for the specified product category.
     *
     * @param category product category
     * @param content  product content to be validated
     * @param cache    optional context-local cache for the validation schemas
     * @return the schema definition entity that was used for validation
     * @throws ContentValidationException if the specified category has no validation schema, if the content is not
     *                                    well-formed and cannot be validated, or if the content is not valid
     */
    SchemaDefinition validate(String category, String content,
                              JobSchemaContext cache) throws ContentValidationException;
}
