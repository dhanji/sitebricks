package com.google.sitebricks.transport;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.client.Transport;

/**
 * 
 */
@ImplementedBy(MutiPartFormTransport.class)
public abstract class MultiPartForm implements Transport {

    public String contentType() {
        return "multipart/form-data";
    }

}
