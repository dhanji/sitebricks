package com.google.sitebricks.example;

import com.google.sitebricks.At;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/error")
public class CompileErrors {
    public String getMessage1() {
        return "";
    }

    public int getMessage2() {
        return 0;
    }

    public String getMessage3() {
        throw new RuntimeException("an exception"); 
    }
}
