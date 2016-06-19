package io.rainfall.utils;

/**
 * @author Aurelien Broszniowski
 */
public class MergeableBitSet {

  private int currentSize = 0;
  private int size;

  public MergeableBitSet(int size) {
    this.size = size;
  }

  public void increase() {
    currentSize++;
  }

  public synchronized boolean isTrue() {
    return size == currentSize;
  }

  public int getCurrentSize() {
    return currentSize;
  }

  public void setTrue() {
    currentSize = size;
  }
}

