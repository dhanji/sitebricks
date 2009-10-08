package com.google.sitebricks;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class NoSuchResourceException extends RuntimeException {
    public NoSuchResourceException(String message) {
        super(message);
    }

    public NoSuchResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
