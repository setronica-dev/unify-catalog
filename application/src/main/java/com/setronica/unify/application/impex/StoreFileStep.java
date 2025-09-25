package com.setronica.unify.application.impex;

import com.setronica.unify.persistence.entity.StoredFile;
import com.setronica.unify.persistence.repository.StoredFileRepository;
import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.AbstractStep;
import org.springframework.core.io.Resource;

/**
 * This simple Step transfers a file from the local FS to cluster-wide storage. The file can then be accessed from any
 * node in the cluster, regardless of which one executed the job that created the file originally.
 */
public class StoreFileStep extends AbstractStep {
    private static final Logger log = LoggerFactory.getLogger(StoreFileStep.class);
    private Resource target;
    private StoredFileRepository storedFileRepository;

    public StoreFileStep(Resource target, StoredFileRepository storedFileRepository, JobRepository jobRepository) {
        this.target = target;
        this.storedFileRepository = storedFileRepository;
        setName("storeFile");
        setJobRepository(jobRepository);
    }

    @Override
    protected void doExecute(StepExecution stepExecution) throws Exception {
        long executionId = stepExecution.getJobExecutionId();
        StoredFile storedFile = storedFileRepository.findById(executionId).orElse(null);
        if (storedFile != null) {
            log.info("Unable to store file: file for execution {} already exists", executionId);
            return;
        }
        storedFile = new StoredFile();
        storedFile.setId(executionId);
        //This is Hibernate-specific internals, if the placeholder implementation is replaced with another LOB, replace
        //this with the preferred ORM's recommended approach.
        storedFile.setData(BlobProxy.generateProxy(target.getInputStream(), target.contentLength()));
        storedFileRepository.save(storedFile);
        log.info("File stored for execution {}", executionId);
    }
}
