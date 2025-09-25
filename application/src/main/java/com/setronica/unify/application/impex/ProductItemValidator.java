package com.setronica.unify.application.impex;

import com.setronica.unify.catalog.domain.Product;
import com.setronica.unify.catalog.exception.ContentValidationException;
import com.setronica.unify.catalog.service.ContentValidationService;
import com.setronica.unify.catalog.service.JobSchemaContext;
import com.setronica.unify.persistence.entity.SchemaDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.util.StringUtils;

/**
 * Executed in the 'processor' part of a {@link org.springframework.batch.core.Step}, via
 * {@link org.springframework.batch.item.validator.ValidatingItemProcessor}.
 * <p/>
 * If a product fails validation, it will be counted as either 'filtered' or 'skipped' in the Step's statistics,
 * depending on the processor's filter flag. The 'filtered' exceptions are discarded, so reporting the error can only
 * be done in this validator, before the exception is thrown. 'Skipped' exceptions, however, would be delivered to an
 * {@link org.springframework.batch.core.ItemProcessListener}, which can then record them for the report.
 */
public class ProductItemValidator implements Validator<Product> {
    private static final Logger log = LoggerFactory.getLogger(ProductItemValidator.class);
    private ContentValidationService service;
    private JobSchemaContext cache;

    public ProductItemValidator(ContentValidationService service) {
        this.service = service;
        this.cache = new JobSchemaContext();
    }

    @Override
    public void validate(Product product) throws ValidationException {
        if (!StringUtils.hasText(product.getIdentifier())
                || !StringUtils.hasText(product.getContent())) {
            throw new ValidationException("Required fields empty");
        }
        try {
            SchemaDefinition schemaDefinition = service.validate(product.getCategory(), product.getContent(), cache);
            product.setSchemaDefinition(schemaDefinition);
        } catch (ContentValidationException e) {
            log.debug("Product {}: validation failed; code={}, errors={}", product.getIdentifier(), e.getCode(), e.getMessages());
            throw new ValidationException("Schema validation failed", e);
        }
    }
}
