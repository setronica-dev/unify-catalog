package com.setronica.unify.catalog.domain;

import com.setronica.unify.persistence.entity.ProductEntity;
import com.setronica.unify.persistence.entity.SchemaDefinition;

import java.util.UUID;

public class Product {
    private UUID id;
    private String identifier;
    private String category;
    private String content;
    private SchemaDefinition schemaDefinition;

    public Product() {
    }

    public Product(ProductEntity item) {
        setId(item.getId());
        setCategory(item.getCategory());
        setIdentifier(item.getIdentifier());
        setContent(item.getContent());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SchemaDefinition getSchemaDefinition() {
        return schemaDefinition;
    }

    public void setSchemaDefinition(SchemaDefinition schemaDefinition) {
        this.schemaDefinition = schemaDefinition;
    }
}
