package com.setronica.unify.application.impex;

import com.setronica.unify.catalog.domain.Product;
import com.setronica.unify.catalog.service.ProductService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * This is a straight {@link ItemWriter} implementation that submits a set of pre-validated products to the relevant
 * service, one by one.
 */
public class ProductItemWriter implements ItemWriter<Product> {
    private ProductService service;

    public ProductItemWriter(ProductService service) {
        this.service = service;
    }

    @Override
    public void write(Chunk<? extends Product> chunk) {
        for (Chunk<? extends Product>.ChunkIterator iterator = chunk.iterator(); iterator.hasNext(); ) {
            Product product = iterator.next();
            try {
                service.saveProduct(product);
            } catch (Exception e) {
                //If a product in the chunk cannot be saved, it is 'skipped', then on completion of the chunk, the
                //item and its exception will be delivered to the {@link org.springframework.batch.core.SkipListener},
                //if one is registered, and, optionally, retried in the next batch, if the Step is configured to do that
                iterator.remove(e);
            }
        }
    }
}
