package com.google.sitebricks.persist;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.matcher.Matcher;

import java.lang.reflect.AnnotatedElement;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public final class PersistAopModule extends AbstractModule {
  private final AbstractPersistenceModule module;

  public PersistAopModule(AbstractPersistenceModule module) {
    this.module = module;
  }

  @Override
  protected void configure() {
    Key<Persister> persisterKey = module.selectorKey(Persister.class);
    WorkInterceptor workInterceptor = new WorkInterceptor(persisterKey);
    TransactionInterceptor transactionInterceptor = new TransactionInterceptor(persisterKey);
    requestInjection(workInterceptor);
    requestInjection(transactionInterceptor);

    Matcher<AnnotatedElement> workMatcher = annotatedWith(Work.class);
    Matcher<AnnotatedElement> txnMatcher = annotatedWith(Transactional.class);

    // Visible persistence APIs.
    if (module.selector != null) {
      workMatcher = workMatcher.and(annotatedWith(module.selector));
      txnMatcher = txnMatcher.and(annotatedWith(module.selector));
    }

    bindInterceptor(any(), workMatcher, workInterceptor);
    bindInterceptor(any(), txnMatcher, transactionInterceptor);
  }
}
