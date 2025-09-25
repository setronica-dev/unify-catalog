package com.setronica.unify.catalog.service;

/**
 * This service can be implemented to deliver external notifications about product and schema update events.
 * <p/>
 * Note: This service will be invoked inside a transaction. If the implementation involves external communication, the
 * event should be stored in the DB first, and delivered to the external system after a successful commit.
 * @see: Outbox pattern
 * @see: {@link org.springframework.transaction.support.TransactionSynchronization}
 */
public interface EventService {
    /**
     * Invoked when a product entity is modified.
     *
     * @param productIdentifier the identifier of the modified product
     */
    void productUpdateEvent(String productIdentifier);

    /**
     * Invoked when a validation schema is modified.
     *
     * @param productCategory the identifier of the modified schema
     */
    void schemaUpdateEvent(String productCategory);
}
