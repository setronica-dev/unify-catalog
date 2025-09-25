package com.setronica.unify.application.dto;

import java.util.List;

public class ValidationResultDto {
    private boolean success;
    private String errorCode;
    private List<ValidationMessageDto> errors;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public List<ValidationMessageDto> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationMessageDto> errors) {
        this.errors = errors;
    }
}
