package com.setronica.unify.application;

import com.setronica.unify.catalog.CoreContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
@Import(CoreContext.class)
public class UnifyCatalogApplication {

    /**
     * This bean enables async job execution.
     */
    @Bean
    @BatchTaskExecutor
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    public static void main(String[] args) {
        SpringApplication.run(UnifyCatalogApplication.class, args);
    }
}
