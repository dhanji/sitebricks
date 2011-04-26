package com.google.sitebricks.mail.imap;

import java.util.List;

/**
 * A command utility that extracts data for particular commands.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
interface Extractor<D> {
  D extract(List<String> messages);
}
