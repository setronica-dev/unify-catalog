package com.setronica.unify.catalog.exception;

/**
 * A basic validation message object. This should have fields that allow machine interpretation of the validation schema
 * assertions, while being relevant to actual business requirements.
 */
public class ValidationErrorMessage {
    /**
     * The type of the validation assertion that produced this message. This could be something like 'missing required
     * attribute' or 'invalid attribute value'.
     */
    private String type;
    /**
     * The attribute that failed the assertion.
     */
    private String property;
    /**
     * The path to the attribute in question, from content root.
     */
    private String path;

    public ValidationErrorMessage(String type, String property, String path) {
        this.type = type;
        this.property = property;
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public String getProperty() {
        return property;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ValidationErrorMessage{" +
                "type='" + type + '\'' +
                ", property='" + property + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
