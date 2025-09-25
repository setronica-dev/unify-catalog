package com.setronica.unify.application.impex;

import com.setronica.unify.catalog.domain.Product;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.FieldSet;

import java.util.Properties;

/**
 * A simple two-way mapper between CSV columns and the domain object, {@link Product}.
 */
public class ProductMapper implements FieldSetMapper<Product>, FieldExtractor<Product> {

    public static final String KEY_ID = "id";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_CONTENT = "content";

    @Override
    public Product mapFieldSet(FieldSet fieldSet) {
        Properties properties = fieldSet.getProperties();
        Product product = new Product();
        product.setIdentifier(properties.getProperty(KEY_ID));
        product.setCategory(properties.getProperty(KEY_CATEGORY));
        product.setContent(properties.getProperty(KEY_CONTENT));
        return product;
    }

    public String[] getHeader() {
        return new String[] {KEY_ID, KEY_CATEGORY, KEY_CONTENT};
    }

    @Override
    public Object[] extract(Product item) {
        return new Object[]{item.getIdentifier(), item.getCategory(), item.getContent()};
    }
}
