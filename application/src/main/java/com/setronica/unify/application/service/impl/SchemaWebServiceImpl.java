package com.setronica.unify.application.service.impl;

import com.setronica.unify.application.dto.ValidationMessageDto;
import com.setronica.unify.application.dto.ValidationResultDto;
import com.setronica.unify.application.service.SchemaWebService;
import com.setronica.unify.catalog.exception.ValidationErrorMessage;
import com.setronica.unify.catalog.exception.SchemaValidationException;
import com.setronica.unify.catalog.service.ContentValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SchemaWebServiceImpl implements SchemaWebService {
    public static final String ERROR_INVALID_SCHEMA = "INVALID_SCHEMA";

    @Autowired
    private ContentValidationService contentValidationService;

    @Override
    public String getSchema(String category) {
        return contentValidationService.getSchema(category);
    }

    @Override
    public ValidationResultDto setSchema(String category, String content) {
        ValidationResultDto dto = new ValidationResultDto();
        try {
            contentValidationService.setSchema(category, content);
            dto.setSuccess(true);
            return dto;
        } catch (SchemaValidationException e) {
            dto.setSuccess(false);
            dto.setErrorCode(ERROR_INVALID_SCHEMA);
            dto.setErrors(new ArrayList(e.getMessages().size()));
            for (ValidationErrorMessage message : e.getMessages()) {
                dto.getErrors().add(new ValidationMessageDto(message.getType(), message.getProperty(), message.getType()));
            }
            return dto;
        }
    }
}
