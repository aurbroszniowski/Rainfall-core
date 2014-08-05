package org.rainfall.generator;

import org.rainfall.SequenceGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class IterationSequenceGenerator implements SequenceGenerator {

  private final AtomicLong next;

  public IterationSequenceGenerator() {
    this.next = new AtomicLong();
  }

  @Override
  public long next() {
    return next.getAndIncrement();
  }
}
