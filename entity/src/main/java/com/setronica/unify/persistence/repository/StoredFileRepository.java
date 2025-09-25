package com.setronica.unify.persistence.repository;

import com.setronica.unify.persistence.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
}
