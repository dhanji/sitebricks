package com.google.sitebricks;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class RespondersForTesting {
    private RespondersForTesting() {
    }

    public static Respond newRespond() {
        return new StringBuilderRespond();
    }
}
