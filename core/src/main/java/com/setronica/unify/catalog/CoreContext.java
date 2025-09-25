package com.setronica.unify.catalog;

import com.setronica.unify.persistence.PersistenceContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import(PersistenceContext.class)
public class CoreContext {
}
