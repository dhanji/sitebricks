package com.google.sitebricks;


/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class PackageScanFailedException extends RuntimeException {
    public PackageScanFailedException(String s, Exception e) {
        super(s, e);
    }
}
