/*
 * Copyright 2014 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rainfall.generator;

import io.rainfall.SequenceGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class IterationSequenceGenerator implements SequenceGenerator {

  static final int BLOCK_SIZE = 1024;

  private final AtomicLong next;
  private final ThreadLocal<LocalRange> localRange = new ThreadLocal<LocalRange>() {
    @Override
    protected LocalRange initialValue() {
      return new LocalRange();
    }
  };

  public IterationSequenceGenerator() {
    this.next = new AtomicLong(1);
  }

  /**
   * Constructor taking the first value to be returned by {@link #next()} in parameter
   *
   * @param firstValue first value to be returned
   */
  public IterationSequenceGenerator(long firstValue) {
    this.next = new AtomicLong(firstValue);
  }

  @Override
  public long next() {
    LocalRange range = localRange.get();
    if (range.next >= range.limit) {
      // Reserving a small per-thread block removes the shared CAS from the hot path,
      // at the cost of possible gaps if a thread stops with unused reserved values.
      long blockStart = next.getAndAdd(BLOCK_SIZE);
      range.next = blockStart;
      range.limit = blockStart + BLOCK_SIZE;
    }
    return range.next++;
  }

  @Override
  public String getDescription() {
    return "Iterative sequence";
  }

  private static class LocalRange {
    private long next;
    private long limit;
  }
}
