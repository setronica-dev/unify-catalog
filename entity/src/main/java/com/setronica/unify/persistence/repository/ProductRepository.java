package com.setronica.unify.persistence.repository;

import com.setronica.unify.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    ProductEntity findByIdentifier(String identifier);
}
