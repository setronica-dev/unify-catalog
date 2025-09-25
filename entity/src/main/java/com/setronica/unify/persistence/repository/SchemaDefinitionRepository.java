package com.setronica.unify.persistence.repository;

import com.setronica.unify.persistence.entity.SchemaDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SchemaDefinitionRepository extends JpaRepository<SchemaDefinition, UUID> {
    @Query("from SchemaDefinition where schema.category = :category and version = schema.latestDefinition")
    SchemaDefinition getLatestDefinition(String category);
}
