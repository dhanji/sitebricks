package com.google.sitebricks.core;

import com.google.sitebricks.rendering.With;
import com.google.sitebricks.rendering.EmbedAs;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@EmbedAs("Case") @With("When")
public class CaseWidget {
    private Object choice;

    public Object getChoice() {
        return choice;
    }

    public void setChoice(Object choice) {
        this.choice = choice;
    }
}
