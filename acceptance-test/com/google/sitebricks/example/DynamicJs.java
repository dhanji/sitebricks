package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.Show;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/dynamic.js") @Show("dynamic.js")
public class DynamicJs {
    public static final String A_MESSAGE = "Hi from warp-sitebricks! (this message was dynamically generated =)";

    public String getMessage() {
        return A_MESSAGE;
    }
}
