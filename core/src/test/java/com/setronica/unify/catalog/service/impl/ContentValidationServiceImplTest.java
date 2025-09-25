package com.setronica.unify.catalog.service.impl;

import com.setronica.unify.catalog.exception.ContentValidationException;
import com.setronica.unify.persistence.entity.SchemaDefinition;
import com.setronica.unify.persistence.repository.SchemaDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ContentValidationServiceImplTest {
    private static final String TEST_CATEGORY = UUID.randomUUID().toString();
    private static final String TEST_SCHEMA = "{" +
            "    \"$id\": \"https://example.com/product.schema.json\"," +
            "    \"$schema\": \"https://json-schema.org/draft/2020-12/schema\"," +
            "    \"title\": \"ProductContent\"," +
            "    \"type\": \"object\"," +
            "    \"properties\": {" +
            "        \"title\": {" +
            "            \"type\": \"string\"," +
            "            \"description\": \"Product title\"" +
            "        }," +
            "        \"description\": {" +
            "            \"type\": \"string\"," +
            "            \"description\": \"Product description\"" +
            "        }," +
            "        \"price\": {" +
            "            \"description\": \"Product price\"," +
            "            \"type\": \"number\"," +
            "            \"minimum\": 0" +
            "        }" +
            "    }," +
            "    \"required\": [\"title\", \"price\"]" +
            "}";

    @Mock
    private SchemaDefinitionRepository repository;

    @Mock
    private ObjectProvider provider;

    @Spy
    @InjectMocks
    private ContentValidationServiceImpl service;

    protected SchemaDefinition definition;

    @BeforeEach
    void setUp() {
        definition = new SchemaDefinition();
        definition.setContent(TEST_SCHEMA);
    }

    @Test
    void validate_valid() throws ContentValidationException {
        Mockito.when(repository.getLatestDefinition(TEST_CATEGORY)).thenReturn(definition);

        SchemaDefinition result = service.validate(TEST_CATEGORY, "{\"title\":\"hello\",\"price\":5.55}");

        assertSame(definition, result);
    }

    @Test
    void validate_malformed() {
        Mockito.when(repository.getLatestDefinition(TEST_CATEGORY)).thenReturn(definition);

        ContentValidationException exception = assertThrows(ContentValidationException.class,
                () -> service.validate(TEST_CATEGORY, "{\"title\":\"hello\":5.55}"));

        assertSame(ContentValidationException.ErrorCode.MALFORMED_CONTENT, exception.getCode());
    }

    @Test
    void validate_invalid() {
        Mockito.when(repository.getLatestDefinition(TEST_CATEGORY)).thenReturn(definition);

        ContentValidationException exception = assertThrows(ContentValidationException.class,
                () -> service.validate(TEST_CATEGORY, "{\"title\":\"hello\"}"));

        assertSame(ContentValidationException.ErrorCode.INVALID_CONTENT, exception.getCode());
    }

    @Test
    void validate_invalid_category() {
        ContentValidationException exception = assertThrows(ContentValidationException.class,
                () -> service.validate(UUID.randomUUID().toString(), "{\"title\":\"hello\",\"price\":5.55}"));

        assertSame(ContentValidationException.ErrorCode.INVALID_CATEGORY, exception.getCode());
    }
}
