package com.setronica.unify.application.impex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.AbstractStep;

import java.io.File;

import static com.setronica.unify.application.impex.Constants.INPUT_FILE_NAME;

/**
 * This simple Step creates a temporary file from the local FS to be used by the following steps, passing the location
 * to the next steps via the job execution context.
 */
public class CreateFileStep extends AbstractStep {
    private static final Logger log = LoggerFactory.getLogger(CreateFileStep.class);

    public CreateFileStep(JobRepository jobRepository) {
        setName("createFile");
        setJobRepository(jobRepository);
    }

    @Override
    protected void doExecute(StepExecution stepExecution) throws Exception {
        File target = File.createTempFile("download.", ".csv");
        stepExecution.getJobExecution().getExecutionContext().put(INPUT_FILE_NAME, target.getAbsolutePath());
        log.info("File prepared for export: {}", target.getAbsolutePath());
    }
}
