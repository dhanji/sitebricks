package com.google.sitebricks.util;

import java.util.LinkedList;

/**
 * Adds elements to a list, will remove oldest items
 * if the maximum specified capacity is exceeded.
 *
 * TODO: extend Queue and make sure all calls to add/offer/addAll etc.. are bounded.
 *
 * Not TheadSafe.
 */
public class BoundedDiscardingList<E> {
  private final LinkedList<E> list = new LinkedList<E>();
  private final int maxlength;

  public BoundedDiscardingList(int maxlength) {
    this.maxlength = maxlength;
  }

  public boolean add(E item) {
    list.add(item);
    if (list.size() > maxlength) {
      list.remove();
    }
    return true;
  }

  public LinkedList<E> list() {
    return list;
  }
}
