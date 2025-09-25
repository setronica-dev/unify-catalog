package com.setronica.unify.catalog.service.impl;

import com.networknt.schema.*;
import com.setronica.unify.catalog.exception.ContentValidationException;
import com.setronica.unify.catalog.exception.SchemaValidationException;
import com.setronica.unify.catalog.exception.ValidationErrorMessage;
import com.setronica.unify.catalog.service.ContentValidationService;
import com.setronica.unify.catalog.service.EventService;
import com.setronica.unify.catalog.service.JobSchemaContext;
import com.setronica.unify.persistence.entity.ProductSchema;
import com.setronica.unify.persistence.entity.SchemaDefinition;
import com.setronica.unify.persistence.repository.ProductSchemaRepository;
import com.setronica.unify.persistence.repository.SchemaDefinitionRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A JSON schema implementation of content validation.
 */
@Service
public class ContentValidationServiceImpl implements ContentValidationService {
    private final ProductSchemaRepository schemaRepository;
    private final SchemaDefinitionRepository definitionRepository;
    private final EventService eventService;
    private final JsonSchemaFactory factory;

    public ContentValidationServiceImpl(ProductSchemaRepository schemaRepository,
                                        SchemaDefinitionRepository repository,
                                        ObjectProvider<EventService> eventServiceProvider) {
        this.schemaRepository = schemaRepository;
        this.definitionRepository = repository;
        this.eventService = eventServiceProvider.getIfAvailable();
        factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    }

    @Override
    public String getSchema(String category) {
        SchemaDefinition definition = definitionRepository.getLatestDefinition(category);
        return definition == null ? null : definition.getContent();
    }

    @Override
    @Transactional
    public void setSchema(String category, String schema) throws SchemaValidationException {
        JsonSchema metaSchema = factory.getSchema(SchemaLocation.of(SchemaId.V202012));
        Set<ValidationMessage> assertions = metaSchema.validate(schema, InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
        if (!assertions.isEmpty()) {
            throw new SchemaValidationException(convertMessages(assertions));
        }

        ProductSchema productSchema = schemaRepository.getByCategory(category);
        if (productSchema == null) {
            productSchema = new ProductSchema();
            productSchema.setCategory(category);
        }
        long version = productSchema.getLatestDefinition() + 1;
        productSchema.setLatestDefinition(version);
        schemaRepository.save(productSchema);

        SchemaDefinition definition = new SchemaDefinition();
        definition.setSchema(productSchema);
        definition.setVersion(version);
        definition.setContent(schema);
        definitionRepository.save(definition);

        if (eventService != null) {
            eventService.schemaUpdateEvent(category);
        }
    }

    @Override
    public SchemaDefinition validate(String category, String content) throws ContentValidationException {
        return validate(category, content, null);
    }

    @Override
    public SchemaDefinition validate(String category, String content,
                                     JobSchemaContext cache) throws ContentValidationException {
        Pair<SchemaDefinition, JsonSchema> schemaPair = cache == null
                ? resolveSchema(category)
                : cache.computeIfAbsent(category, this::resolveSchema);
        if (schemaPair == null) {
            throw new ContentValidationException(ContentValidationException.ErrorCode.INVALID_CATEGORY, null);
        }

        Set<ValidationMessage> messages;
        try {
            messages = schemaPair.getSecond().validate(content, InputFormat.JSON);
        } catch (IllegalArgumentException e) {
            throw new ContentValidationException(ContentValidationException.ErrorCode.MALFORMED_CONTENT, null);
        }
        if (messages.isEmpty()) {
            return schemaPair.getFirst();
        }
        throw new ContentValidationException(ContentValidationException.ErrorCode.INVALID_CONTENT, convertMessages(messages));
    }

    private Pair<SchemaDefinition, JsonSchema> resolveSchema(String category) {
        SchemaDefinition definition = definitionRepository.getLatestDefinition(category);
        if (definition == null) {
            return null;
        }
        JsonSchema schema = factory.getSchema(definition.getContent());
        return Pair.of(definition, schema);
    }

    private List<ValidationErrorMessage> convertMessages(Set<ValidationMessage> messages) {
        List<ValidationErrorMessage> response = new ArrayList<>(messages.size());
        for (ValidationMessage message : messages) {
            response.add(new ValidationErrorMessage(message.getType(),
                    message.getProperty(),
                    message.getEvaluationPath().toString()));
        }
        return response;
    }
}
