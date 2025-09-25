package com.setronica.unify.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * This entity holds a single, immutable revision of a content validation schema.
 */
@Entity
public class SchemaDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private ProductSchema schema;

    @Column(nullable = false, updatable = false)
    private long version;

    @Column(nullable = false, updatable = false, columnDefinition = "text")
    private String content;

    public UUID getId() {
        return id;
    }

    public ProductSchema getSchema() {
        return schema;
    }

    public void setSchema(ProductSchema schema) {
        this.schema = schema;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
