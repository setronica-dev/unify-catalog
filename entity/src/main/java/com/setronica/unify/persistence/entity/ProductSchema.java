package com.setronica.unify.persistence.entity;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

/**
 * This entity defines a category of products and holds the versioned validation schema for them.
 */
@Entity
public class ProductSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String category;

    @Column
    private long latestDefinition;

    @OneToMany(mappedBy = "schema", fetch = FetchType.LAZY)
    private List<SchemaDefinition> definitions;

    public UUID getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getLatestDefinition() {
        return latestDefinition;
    }

    public void setLatestDefinition(long latestDefinition) {
        this.latestDefinition = latestDefinition;
    }

    public List<SchemaDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<SchemaDefinition> definitions) {
        this.definitions = definitions;
    }
}
