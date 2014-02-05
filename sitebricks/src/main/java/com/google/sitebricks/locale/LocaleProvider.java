package com.google.sitebricks.locale;

import java.util.Locale;

/**
 * Provides the {@link java.util.Locale} for the internationalization.
 */
public interface LocaleProvider {

	/**
	 * Retrieves the locale that is to be used for the i18n of the translatable messages.
	 *
	 * @return the requested {@link java.util.Locale}.
	 */
	Locale getLocale();

}
