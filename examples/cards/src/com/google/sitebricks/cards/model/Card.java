package com.google.sitebricks.cards.model;

import java.util.Date;

/**
 * A data model object representing a card.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Card {
  private String title;
  private String text;
  private Date createdOn;
  private String author;

  public Card(String title, String text, Date createdOn, String author) {
    this.title = title;
    this.text = text;
    this.createdOn = createdOn;
    this.author = author;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
