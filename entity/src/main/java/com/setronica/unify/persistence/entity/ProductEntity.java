package com.setronica.unify.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * This entity carries the actual business-relevant data. This sample implementation is, effectively, a key-value
 * storage with schema validation, reimplemented as an RDB.
 */
@Entity
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private String identifier;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    private SchemaDefinition schema;

    public UUID getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public SchemaDefinition getSchema() {
        return schema;
    }

    public void setSchema(SchemaDefinition schema) {
        this.schema = schema;
    }
}
