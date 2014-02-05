/**
 * This file is part of the source code and related artifacts for eGym Application.
 *
 * Copyright © 2013 eGym GmbH
 */
package com.google.sitebricks.locale;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provides a default implementation of the {@link LocaleProvider}. It retrieves the Locale as stored in the
 * {@link javax.servlet.http.HttpServletRequest}.
 */
class LocaleProviderImpl implements LocaleProvider {

	private final Provider<HttpServletRequest> requestProvider;

	@Inject
	LocaleProviderImpl(final Provider<HttpServletRequest> requestProvider) {
		this.requestProvider = requestProvider;
	}

	/**
	 * @return the Locale as stored in the {@link javax.servlet.http.HttpServletRequest}.
	 */
	public Locale getLocale() {
		if (requestProvider == null) {
			throw new IllegalStateException("The HttpServletRequest provider must be bound.");
		}

		return requestProvider.get().getLocale();
	}
}
