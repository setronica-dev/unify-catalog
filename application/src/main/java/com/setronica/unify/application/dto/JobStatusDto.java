package com.setronica.unify.application.dto;

public enum JobStatusDto {
    /**
     * The job is in progress.
     */
    RUNNING,
    /**
     * The job is complete.
     */
    COMPLETE,
    /**
     * The job could not be fully completed and must be started again.
     */
    FAILED,
    /**
     * The job referenced by the token does not exist or is not available.
     */
    UNKNOWN,
}
