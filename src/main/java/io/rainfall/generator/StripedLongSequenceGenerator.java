/*
 * Copyright (c) 2014-2022 Aurélien Broszniowski
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
import io.rainfall.configuration.DistributedConfig;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Striped {@link SequenceGenerator} that divides the range of generated sequences into <code>instanceCount</code>
 * sub-ranges.
 * <p>
 * This generator is useful in scenarios where multiple independent JVMs are being used and need
 * to generate unique sequences as each JVM will get its own sub-range to generate from, provided that each JVM
 * supplies a distinct index with the given {@link InstanceIndexSupplier}.
 * <p>
 * Indices supplied by {@link InstanceIndexSupplier} must be in the range <code>0 to (instanceCount - 1)</code>.
 */
public class StripedLongSequenceGenerator implements SequenceGenerator {

  private final AtomicLong next;
  private final long begin;
  private final long end;

  public StripedLongSequenceGenerator(final DistributedConfig distributedConfig) {
    this(distributedConfig.getNbClients(), new InstanceIndexSupplier() {
      @Override
      public Integer get() {
        return distributedConfig.getCurrentClientId();
      }
    });
  }

  public StripedLongSequenceGenerator(int instanceCount, InstanceIndexSupplier instanceIndexSupplier) {
    this(instanceCount, instanceIndexSupplier, Long.MAX_VALUE);
  }

  public StripedLongSequenceGenerator(int instanceCount, InstanceIndexSupplier instanceIndexSupplier, long totalRange) {
    int instanceIndex = instanceIndexSupplier.get();
    if (instanceIndex < 0) {
      throw new IllegalArgumentException("Supplied instance index '" + instanceIndex + "' is lower than 0");
    }
    if (instanceIndex >= instanceCount) {
      throw new IllegalArgumentException("Supplied instance index '" + instanceIndex + "' is higher than instance count of " + instanceCount);
    }
    long localRange = totalRange / instanceCount;
    this.begin = instanceIndex * localRange;
    this.end = begin + localRange - 1;
    this.next = new AtomicLong(begin);
  }

  @Override
  public long next() {
    while (true) {
      long value = next.getAndIncrement();
      if (value <= end) {
        return value;
      }
      if (value > end) {
        next.set(begin);
      }
      next.compareAndSet(value + 1, begin);
    }
  }

  @Override
  public String getDescription() {
    return "StripedLong sequence between " + begin + " and " + end;
  }

  public interface InstanceIndexSupplier {
    Integer get();
  }

}
