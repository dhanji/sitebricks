package com.google.sitebricks.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Adds elements to a list, will remove oldest items
 * if the maximum specified capacity is exceeded.
 *
 * TheadSafe & concurrent.
 */
public class BoundedDiscardingList<E> {
  private final Queue<E> queue = new ConcurrentLinkedQueue<E>();
  private final int maxlength;

  public BoundedDiscardingList(int maxlength) {
    this.maxlength = maxlength;
  }

  public boolean add(E item) {
    queue.add(item);
    if (queue.size() > maxlength) {
      queue.poll();
    }
    return true;
  }

  /**
   * Returns a snapshot of the list at *some* point in time.
   */
  public List<E> list() {
    return Lists.newArrayList(queue);
  }
}
