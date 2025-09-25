package com.setronica.unify.persistence.repository;

import com.setronica.unify.persistence.entity.ProductSchema;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.UUID;

public interface ProductSchemaRepository extends JpaRepository<ProductSchema, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ProductSchema getByCategory(String category);
}
