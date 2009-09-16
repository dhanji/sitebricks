package com.google.sitebricks.cards;

import com.google.sitebricks.cards.model.Card;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Finds and manages cards for us.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class CardDao {

  public List<Card> list() {

    // Return a mock set as an example.
    return Arrays.asList(
        new Card("hello", "how are you", new Date(), "dhanji"),
        new Card("yoyo", "how are you dude?", new Date(), "zac")
    );
  }
}
