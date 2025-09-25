package com.setronica.unify.application.impex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.io.File;

/**
 * This job listener is used to delete a temporary File after the job (that's working on that file) finishes or fails.
 * It's not completely reliable, but it will discard the file much sooner than {@link java.io.File#deleteOnExit}, and
 * with a smaller memory footprint for active nodes.
 */
public class DeleteFileListener implements JobExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(DeleteFileListener.class);

    private String jobParamName;
    private String jobContextName;

    public DeleteFileListener(String jobParamName, String jobContextName) {
        this.jobParamName = jobParamName;
        this.jobContextName = jobContextName;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobParamName != null) {
            String string = jobExecution.getJobParameters().getString(jobParamName);
            log.info("Checking job param '{}': {}", jobParamName, string);
            if (string != null) {
                deleteFile(string);
            }
        }
        if (jobContextName != null) {
            String string = jobExecution.getExecutionContext().getString(jobContextName);
            log.info("Checking job context '{}': {}", jobParamName, string);
            if (string != null) {
                deleteFile(string);
            }
        }
    }

    private void deleteFile(String target) {
        try {
            File file = new File(target);
            if (file.isFile()) {
                file.delete();
            }
            log.info("File deleted: {}", target);
        } catch (Exception e) {
            log.error("Failed to delete file for name {}", target, e);
        }
    }
}
