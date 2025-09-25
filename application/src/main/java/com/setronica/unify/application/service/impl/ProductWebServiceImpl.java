package com.setronica.unify.application.service.impl;

import com.setronica.unify.application.dto.ProductDto;
import com.setronica.unify.application.dto.ValidationMessageDto;
import com.setronica.unify.application.dto.ValidationResultDto;
import com.setronica.unify.application.service.ProductWebService;
import com.setronica.unify.catalog.domain.Product;
import com.setronica.unify.catalog.exception.ContentValidationException;
import com.setronica.unify.catalog.exception.ValidationErrorMessage;
import com.setronica.unify.catalog.service.ContentValidationService;
import com.setronica.unify.catalog.service.ProductService;
import com.setronica.unify.persistence.entity.SchemaDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class ProductWebServiceImpl implements ProductWebService {
    @Autowired
    private ProductService productService;
    @Autowired
    private ContentValidationService contentValidationService;

    @Override
    public ProductDto getProduct(String identifier) {
        Product product = productService.getProduct(identifier);
        return toDto(product);
    }

    @Override
    @Transactional
    public ValidationResultDto setProduct(String identifier, ProductDto content) {
        SchemaDefinition definition;
        try {
            definition = contentValidationService.validate(content.getCategory(), content.getContent());
        } catch (ContentValidationException e) {
            ValidationResultDto dto = new ValidationResultDto();
            dto.setSuccess(false);
            dto.setErrorCode(e.getCode().toString());
            dto.setErrors(new ArrayList(e.getMessages().size()));
            for (ValidationErrorMessage message : e.getMessages()) {
                dto.getErrors().add(new ValidationMessageDto(message.getType(), message.getProperty(), message.getType()));
            }
            return dto;
        }

        productService.saveProduct(toDomain(identifier, content, definition));

        ValidationResultDto dto = new ValidationResultDto();
        dto.setSuccess(true);
        return dto;
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setCategory(product.getCategory());
        dto.setContent(product.getContent());
        return dto;
    }

    private Product toDomain(String identifier, ProductDto content, SchemaDefinition definition) {
        Product product = new Product();
        product.setIdentifier(identifier);
        product.setCategory(content.getCategory());
        product.setContent(content.getContent());
        product.setSchemaDefinition(definition);
        return product;
    }
}
