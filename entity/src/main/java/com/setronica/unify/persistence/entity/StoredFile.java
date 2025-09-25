package com.setronica.unify.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import java.sql.Blob;

/**
 * This is a placeholder table for file sharing in a clustered environment. If available, a dedicated file storage,
 * like S3, would perform this role better than an SQL BLOB.
 */
@Entity
public class StoredFile {

    @Id
    private long id;

    @Lob
    private Blob data;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Blob getData() {
        return data;
    }

    public void setData(Blob data) {
        this.data = data;
    }
}
