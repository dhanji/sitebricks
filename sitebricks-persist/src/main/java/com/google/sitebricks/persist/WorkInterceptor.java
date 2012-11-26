package com.google.sitebricks.persist;

import com.google.inject.Injector;
import com.google.inject.Key;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;

/**
 * Guice AOP interceptor that manages unit of work semantics.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class WorkInterceptor implements MethodInterceptor {
  private final Key<Persister> persisterKey;
  private Persister persister;

  public WorkInterceptor(Key<Persister> persisterKey) {
    this.persisterKey = persisterKey;
  }

  @Inject
  public void init(Injector injector) {
    this.persister = injector.getInstance(persisterKey);
  }

  @Override
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
    return persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore entityStore) throws Throwable {
        return methodInvocation.proceed();
      }
    });
  }
}
