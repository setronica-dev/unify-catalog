package com.setronica.unify.catalog.exception;

import java.util.Collections;
import java.util.List;

public class ContentValidationException extends Exception {
    /**
     * The top-level type of error
     */
    private ErrorCode code;
    /**
     * Messages explaining the error in detail (optional).
     */
    private List<ValidationErrorMessage> messages;

    public ContentValidationException(ErrorCode code, List<ValidationErrorMessage> messages) {
        this.code = code;
        this.messages = messages == null ? Collections.emptyList() : Collections.unmodifiableList(messages);
    }

    public ErrorCode getCode() {
        return code;
    }

    public List<ValidationErrorMessage> getMessages() {
        return messages;
    }

    public enum ErrorCode {
        INVALID_CATEGORY,
        MALFORMED_CONTENT,
        INVALID_CONTENT,
    }
}
