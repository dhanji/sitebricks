package com.google.sitebricks.locale;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class LocaleProviderModule extends AbstractModule {
	@Override
	protected final void configure() {
		bind(LocaleProvider.class).to(LocaleProviderImpl.class).in(Singleton.class);
	}
}
