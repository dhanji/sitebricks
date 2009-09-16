package com.google.sitebricks.cards;

import com.google.inject.Inject;
import com.google.sitebricks.cards.model.Card;
import com.google.sitebricks.http.Get;

import java.util.List;

/**
 * The cards application home page, lists cards.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Home {

  private List<Card> cards;

  @Inject
  private CardDao cardDao;

  @Get
  public void get() {
    // Load the list of cards in this HTTP get handler...
    cards = cardDao.list();
  }


  // Getters + Setters...
  public List<Card> getCards() {
    return cards;
  }
}
