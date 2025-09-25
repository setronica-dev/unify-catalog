package com.setronica.unify.catalog.service;

import com.networknt.schema.JsonSchema;
import com.setronica.unify.persistence.entity.SchemaDefinition;
import org.springframework.data.util.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This context object is free to use any implementation or approach for the cache, as long as it is thread-safe.
 * Limiting max cache size or sharing the cache between different instances of the context is possible, if the memory
 * footprint can become a concern and fixing schemas per job is not one.
 */
public class JobSchemaContext {
    private final Map<String, Pair<SchemaDefinition, JsonSchema>> map;

    public JobSchemaContext() {
        this.map = new ConcurrentHashMap<>();
    }

    public Pair<SchemaDefinition, JsonSchema> computeIfAbsent(String key,
                                                              Function<String, Pair<SchemaDefinition, JsonSchema>> f) {
        return map.computeIfAbsent(key, f);
    }
}
