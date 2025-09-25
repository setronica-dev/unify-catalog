package com.setronica.unify.application.impex;

import com.setronica.unify.catalog.domain.Product;
import com.setronica.unify.catalog.service.ContentValidationService;
import com.setronica.unify.catalog.service.ProductService;
import com.setronica.unify.persistence.entity.ProductEntity;
import com.setronica.unify.persistence.repository.StoredFileRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Core configuration for the Spring Batch integration
 */
@Configuration
public class BatchJobConfiguration {
    public static final String PARAM_INPUT_FILE_NAME = "#{jobParameters['" + Constants.INPUT_FILE_NAME + "']}";
    public static final String CONTEXT_INPUT_FILE_NAME = "#{jobExecutionContext['" + Constants.INPUT_FILE_NAME + "']}";
    public static final String TARGET_FILE_RESOURCE = "targetFileResource";
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private StoredFileRepository storedFileRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private ContentValidationService contentValidationService;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * This bean declares the structure and components of the import job.
     * <p/>
     * Input parameters:<p/>
     * input.file.name: the path to the source file on the local FS
     */
    @Bean
    public Job importJob(TaskletStep importStep) {
        return new JobBuilder("importJob", jobRepository)
                .start(importStep)
                .listener(new DeleteFileListener(Constants.INPUT_FILE_NAME, null))
                .build();
    }

    /**
     * The core step of the import job. The writer is stateless and is instantiated here, but the reader and processor
     * are not, so they are declared as separate {@link StepScope} beans.<p/>
     * Configuration of this bean controls all primary aspects of the workflow: batch size / transaction length,
     * parallelism, retries and error reporting.
     */
    @Bean
    public TaskletStep importStep(FlatFileItemReader reader,
                                  ValidatingItemProcessor validatingItemProcessor,
                                  @Value("${batch.import.job.chunk.size:100}") int chunkSize) {
        return new StepBuilder("importStep", jobRepository)
                .<Product, Product>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(validatingItemProcessor)
                .writer(new ProductItemWriter(productService))
                .faultTolerant()
                .build();
    }

    /**
     * This bean declares the structure and components of the export job.
     * <p/>
     * Input parameters:<p/>
     * none
     */
    @Bean
    public Job exportJob(TaskletStep exportStep, StoreFileStep storeFileStep) {
        return new JobBuilder("exportJob", jobRepository)
                .start(new CreateFileStep(jobRepository))
                .next(exportStep)
                .next(storeFileStep)
                .listener(new DeleteFileListener(null, Constants.INPUT_FILE_NAME))
                .build();
    }

    /**
     * This is the primary step of the export job. The transformation processor is stateless, but reader and writer are
     * not, so they are declared as separate {@link StepScope} beans.
     */
    @Bean
    public TaskletStep exportStep(FlatFileItemWriter<Product> writer,
                                  JpaPagingItemReader<ProductEntity> reader) {
        return new StepBuilder("exportStep", jobRepository)
                .<ProductEntity, Product>chunk(100, transactionManager)
                .reader(reader)
                .processor(Product::new)
                .writer(writer)
                .faultTolerant()
                .build();
    }

    /**
     * This post-processing step transfers the export file from the local FS to cluster-wide storage.<p/>
     * In this example of late binding, the Step bean cannot be declared with a scope, even though its constructor
     * requires job-scope data, so that data is itself declared as a StepScope bean.
     */
    @Bean
    public StoreFileStep storeFileStep(@Qualifier(TARGET_FILE_RESOURCE) Resource resource) {
        return new StoreFileStep(resource, storedFileRepository, jobRepository);
    }

    /**
     * The import ItemReader, preconfigured to import feeds of a certain format from a CSV file. The core component
     * is not thread-safe or reusable, so a new instance is created for each execution.
     *
     * @param name late binding for the import file name
     */
    @Bean
    @StepScope
    public FlatFileItemReader flatFileItemReader(@Value(PARAM_INPUT_FILE_NAME) String name) {
        //LineTokenizer splits a single 'line' of the source file into a FieldSet, making the raw data available as an
        //array of Strings or a Map<String, String>, if the header names are configured. This tokenizer reasonably
        //handles CSV quoting OOB.
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();

        //LineMapper wraps the LineTokenizer to follow it with a Mapper, converting the FieldSet into a domain object
        DefaultLineMapper lineMapper = new DefaultLineMapper();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new ProductMapper());

        //The FlatFileItemReader is not CSV-compatible OOB, but can be configured to reasonably approximate a CSV parser
        FlatFileItemReader reader = new FlatFileItemReader();
        reader.setResource(new FileSystemResource(name));
        //This is the part that "parses" the CSV header; skipping the first line feeds it into the SkippedLinesCallback,
        //which then uses the LineTokenizer to parse it and feed the resulting column names right back to the Tokenizer,
        //to be used as headers for the following lines
        reader.setLinesToSkip(1);
        reader.setSkippedLinesCallback(skippedLine -> tokenizer.setNames(tokenizer.tokenize(skippedLine).getValues()));
        //The oddly-named DefaultRecordSeparatorPolicy _isn't_, and the actual default does not support properly quoted
        //multi-line CSV records.
        reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
        reader.setLineMapper(lineMapper);
        return reader;
    }

    /**
     * The 'processor' part of the import step, a validator that applies the relevant validation schemas to product
     * content provided in the file. The ValidatingItemProcessor itself is stateless, but the ProductItemValidator
     * includes a job-local cache of validation schemas.
     */
    @Bean
    @StepScope
    public ValidatingItemProcessor validatingItemProcessor() {
        ValidatingItemProcessor processor = new ValidatingItemProcessor();
        processor.setFilter(true);
        processor.setValidator(new ProductItemValidator(contentValidationService));
        return processor;
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<ProductEntity> productEntityJpaItemReader() {
        JpaPagingItemReader<ProductEntity> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("from ProductEntity order by identifier");
        return reader;
    }

    /**
     * The ItemWriter converts the domain object to an array of Strings and writes it to a CSV file.
     *
     * @param resource reference to the local FS file created by a previous step
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<Product> productFlatFileItemWriter(@Qualifier(TARGET_FILE_RESOURCE) WritableResource resource) {
        //Just the FieldExtractor, to convert a Product into a simple array
        ProductMapper mapper = new ProductMapper();

        //OOB, the LineAggregators included with the library cannot produce properly quoted CSV. The closest one,
        //DelimitedLineAggregator, just adds the quote character to the beginning and end of each string, escaping
        //nothing in the string's content; naturally, it will not produce a file that could be imported back again
        SimpleCsvLineAggregator<Product> aggregator = new SimpleCsvLineAggregator<>(",", '\"');
        aggregator.setFieldExtractor(mapper);

        FlatFileItemWriter<Product> writer = new FlatFileItemWriter<>();
        writer.setResource(resource);
        writer.setLineAggregator(aggregator);
        //writing the header is needlessly complicated, but at least it works
        writer.setHeaderCallback(w -> w.write(aggregator.doAggregate(mapper.getHeader())));
        return writer;
    }

    @Bean(name = TARGET_FILE_RESOURCE)
    @StepScope
    public FileSystemResource targetFileResource(@Value(CONTEXT_INPUT_FILE_NAME) String name) {
        return new FileSystemResource(name);
    }
}
