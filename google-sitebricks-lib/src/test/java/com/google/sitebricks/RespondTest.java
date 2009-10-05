package com.google.sitebricks;

import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class RespondTest {
    private static final String A_STRING = "aoskpoaksdas";
    private static final char A_CHAR = 'h';

    @Test
    public final void respondWriteStringCharAndChew() {
        final Respond respond = new StringBuilderRespond();
        respond.write(A_STRING);
        respond.write(A_CHAR);
        respond.write(A_CHAR);
        respond.chew();

        assert (A_STRING + A_CHAR).equals(respond.toString());
    }

    @Test
    public final void respondWriteNull() {
        final Respond respond = new StringBuilderRespond();
        respond.write(null);

        assert ("" + null).equals(respond.toString());
    }
}
