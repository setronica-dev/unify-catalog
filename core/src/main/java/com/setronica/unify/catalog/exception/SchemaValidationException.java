package com.setronica.unify.catalog.exception;

import java.util.Collections;
import java.util.List;

public class SchemaValidationException extends Exception {
    private List<ValidationErrorMessage> messages;

    public SchemaValidationException(List<ValidationErrorMessage> messages) {
        this.messages = Collections.unmodifiableList(messages);
    }

    public List<ValidationErrorMessage> getMessages() {
        return messages;
    }
}
