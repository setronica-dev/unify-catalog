package com.setronica.unify.catalog.service.impl;

import com.setronica.unify.catalog.domain.Product;
import com.setronica.unify.catalog.service.EventService;
import com.setronica.unify.catalog.service.ProductService;
import com.setronica.unify.persistence.entity.ProductEntity;
import com.setronica.unify.persistence.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired(required = false)
    private EventService eventService;

    @Override
    public Product getProduct(String identifier) {
        ProductEntity product = productRepository.findByIdentifier(identifier);
        return product == null ? null : new Product(product);
    }

    @Override
    public void saveProduct(Product product) {
        String identifier = product.getIdentifier();
        if (!StringUtils.hasText(identifier)
                || !StringUtils.hasText(product.getCategory())
                || !StringUtils.hasText(product.getContent())
                || product.getSchemaDefinition() == null) {
            throw new IllegalArgumentException("All product fields are required");
        }

        //This method makes only a basic effort to avoid conflicts when inserting new items into the DB. In a batch
        //setting, multiple threads could attempt to insert the same products in parallel batches, with the DB-side
        //constraint violation exception delayed until the end of the transaction block. There are multiple ways to
        //quietly and successfully upsert conflicting items in such a scenario, depending on the DB and extra tools
        //available; the _easiest_ path for an SQL DB may be to avoid transactions and catch the exception on save.
        ProductEntity entity = productRepository.findByIdentifier(identifier);
        if (entity == null) {
            entity = new ProductEntity();
            entity.setIdentifier(identifier);
        }

        //Unless it somehow contradicts the business requirements, skipping no-op writes -- where the entity is
        //completely unchanged after a commit -- is a great time-and-space-saving measure. It doesn't even have
        //to be in-depth to be beneficial, comparing the serialized form of an object will still save resources
        //(as long as the serialization is deterministic _and_ the outside data source does not provide a delta
        //feed). The business requirements would probably _require_ skipping no-op writes for a non-incremental
        //source, likely including skipping logically equivalent content, not just identical.
        entity.setCategory(product.getCategory());
        entity.setContent(product.getContent());
        entity.setSchema(product.getSchemaDefinition());
        productRepository.save(entity);

        if (eventService != null) {
            eventService.productUpdateEvent(identifier);
        }
    }
}
