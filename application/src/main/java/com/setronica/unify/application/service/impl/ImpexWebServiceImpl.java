package com.setronica.unify.application.service.impl;

import com.setronica.unify.application.dto.JobStatisticsDto;
import com.setronica.unify.application.dto.JobStatusDto;
import com.setronica.unify.application.impex.Constants;
import com.setronica.unify.application.service.ImpexWebService;
import com.setronica.unify.application.util.DeleteFileOnCloseInputStream;
import com.setronica.unify.application.util.PresizedInputStreamResource;
import com.setronica.unify.persistence.entity.StoredFile;
import com.setronica.unify.persistence.repository.StoredFileRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Collection;
import java.util.UUID;

@Service
public class ImpexWebServiceImpl implements ImpexWebService {
    private static final Logger log = LoggerFactory.getLogger(ImpexWebServiceImpl.class);
    public static final String PARAM_INSTANCE = "instance";

    @Autowired
    @Qualifier("importJob")
    private Job importJob;

    @Autowired
    @Qualifier("exportJob")
    private Job exportJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private StoredFileRepository storedFileRepository;

    @Override
    public long uploadFile(MultipartFile value) throws IOException {
        File target = File.createTempFile("upload.", ".csv");
        try (InputStream in = value.getInputStream();
             OutputStream out = new FileOutputStream(target)) {
            IOUtils.copy(in, out);
        } catch (Exception e) {
            target.delete();
            throw e;
        }

        JobParametersBuilder builder = prepareJobParameters();
        builder.addString(Constants.INPUT_FILE_NAME, target.getAbsolutePath());
        try {
            JobExecution execution = jobLauncher.run(importJob, builder.toJobParameters());
            if (execution.getId() == null) {
                throw new AssertionError();
            }
            return execution.getId();
        } catch (JobExecutionException e) {
            target.delete();
            log.error("Job execution failed for file upload", e);
            throw new IOException(e);
        } catch (Exception e) {
            target.delete();
            throw e;
        }
    }

    @Override
    public long downloadFile() throws IOException {
        JobParametersBuilder builder = prepareJobParameters();
        try {
            JobExecution execution = jobLauncher.run(exportJob, builder.toJobParameters());
            return execution.getId();
        } catch (JobExecutionException e) {
            log.error("Job execution failed for file upload", e);
            throw new IOException(e);
        }
    }

    @Override
    public JobStatusDto getStatus(long token) {
        JobExecution execution = jobExplorer.getJobExecution(token);
        if (execution == null) {
            return JobStatusDto.UNKNOWN;
        }
        if (execution.getStatus().isRunning()) {
            return JobStatusDto.RUNNING;
        }
        if (execution.getStatus() == BatchStatus.COMPLETED) {
            return JobStatusDto.COMPLETE;
        }
        return JobStatusDto.FAILED;
    }

    @Override
    public JobStatisticsDto getStatistics(long token) {
        JobExecution execution = jobExplorer.getJobExecution(token);
        if (execution == null) {
            return null;
        }

        Collection<StepExecution> executions = execution.getStepExecutions();
        JobStatisticsDto dto = new JobStatisticsDto();
        for (StepExecution step : executions) {
            dto.setReadCount(dto.getReadCount() + step.getReadCount());
            dto.setWriteCount(dto.getWriteCount() + step.getWriteCount());
            dto.setFailedCount(dto.getFailedCount() + step.getFilterCount() + step.getSkipCount());
        }
        return dto;
    }

    @Override
    @Transactional
    public Resource getResult(long token) throws IOException {
        StoredFile file = storedFileRepository.findById(token).orElse(null);
        if (file == null) {
            return null;
        }
        File target = File.createTempFile("download.", ".csv");
        try (InputStream in = file.getData().getBinaryStream();
             OutputStream out = new FileOutputStream(target)) {
            IOUtils.copy(in, out);
        } catch (Exception e) {
            target.delete();
            throw new IOException(e);
        }
        return new PresizedInputStreamResource(new DeleteFileOnCloseInputStream(target), target.length());
    }

    @Override
    public void deleteResult(long token) {
        storedFileRepository.deleteById(token);
    }

    private JobParametersBuilder prepareJobParameters() {
        JobParametersBuilder builder = new JobParametersBuilder();
        //this 'identifying` parameter with a unique value allows running a job multiple times, including at the same
        //time, regardless of any other job parameters
        builder.addString(PARAM_INSTANCE, UUID.randomUUID().toString(), true);
        return builder;
    }
}
