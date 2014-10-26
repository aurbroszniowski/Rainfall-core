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

package org.rainfall.statistics;

import org.rainfall.TestException;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver<K extends Enum<K>> {

  private final K[] keys;
  private final Statistics<K> statistics;
  private long timestamp;
  private Long previousCounter = 0L;

  public StatisticsObserver(final Class<K> results) {
    this.keys = results.getEnumConstants();
    this.statistics = new Statistics<K>(results.getEnumConstants());
  }

  protected long getTime() {
    return System.nanoTime();
  }

  public void measure(Task<K> task) throws TestException {
    try {
      final long start = getTime();
      final K result = task.definition();
      final long end = getTime();
      final long latency = (end - start);
      this.timestamp = start / 1000000L;
      this.statistics.increaseCounterAndSetLatency(result, latency);
    } catch (Exception e) {
      throw new TestException("Exception in measured task " + task.toString(), e);
    }
  }

  public K[] getKeys() {
    return keys;
  }

  public StatisticsHolder<K> peek() {
    return new StatisticsHolder<K>(this.timestamp, statistics);
  }

  public boolean hasEmptyQueue() {
    if (this.previousCounter.equals(this.statistics.sumOfCounters())) {
      return true;
    }
    this.previousCounter = this.statistics.sumOfCounters();
    return false;
  }
}
