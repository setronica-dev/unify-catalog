package com.setronica.unify.application.service;

import com.setronica.unify.application.dto.JobStatisticsDto;
import com.setronica.unify.application.dto.JobStatusDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * This service exposes asynchronous operations with batches of products, import and export via CSV files.
 */
public interface ImpexWebService {
    /**
     * Start an asynchronous import job with the provided file as the source. The method will return a unique token
     * that can be used to check the status or progress of the job.
     *
     * @param file the CSV file to be imported
     * @return the unique token
     * @throws IOException if the job could not be started due to a technical issue
     */
    long uploadFile(MultipartFile file) throws IOException;

    /**
     * Start an asynchronous export job, that will create a CSV file with all currently existing products. The method
     * will return a unique token that can be used to check the status or progress of the job.
     *
     * @return the unique token
     * @throws IOException if the job could not be started due to a technical issue
     */
    long downloadFile() throws IOException;

    /**
     * Provides the current status of the job identified by the unique token. Poll this endpoint until it no longer
     * returns {@link JobStatusDto#RUNNING}.
     *
     * @param token the unique token provided when the job was initially started
     * @return the current status of the job
     */
    JobStatusDto getStatus(long token);

    /**
     * Provides some basic statistics about the job's progress.
     *
     * @param token the unique token provided when the job was initially started
     * @return job's stats
     */
    JobStatisticsDto getStatistics(long token);

    /**
     * If the job produces a file as the primary goal or as a secondary report, the file may be downloaded with this.
     *
     * @param token the unique token provided when the job was initially started
     * @return a resource reference to the result or null
     */
    Resource getResult(long token) throws IOException;

    /**
     * If the job produces a file as the primary goal or as a secondary report, the file may be deleted with this.
     *
     * @param token the unique token provided when the job was initially started
     */
    void deleteResult(long token);
}
