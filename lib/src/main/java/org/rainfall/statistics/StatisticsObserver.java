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

import jsr166e.ConcurrentHashMapV8;

import java.util.Comparator;
import java.util.List;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver<K extends Enum<K>> {

  private final K[] keys;
  private final MeldablePriorityQueue<Long, Statistics<K>> statisticsQueue;

  public StatisticsObserver(final Class<K> results) {
    this.keys = results.getEnumConstants();
    statisticsQueue = new MeldablePriorityQueue<Long, Statistics<K>>(new Comparator<Statistics<K>>() {
      @Override
      public int compare(final Statistics<K> o1, final Statistics<K> o2) {
        return o1.getTimestamp().compareTo(o2.getTimestamp());
      }
    });
  }

  protected long getTime() {
    return System.nanoTime();
  }

  public void measure(Task<K> task) {
    try {
      final long start = getTime();
      final K result = task.definition();
      final long end = getTime();
      final long latency = (end - start);
      long timestamp = start/1000000L;
      this.statisticsQueue.meld(timestamp,
          new Statistics<K>(keys, timestamp, result, latency), new ConcurrentHashMapV8.BiFun<Statistics<K>, Statistics<K>, Statistics<K>>() {
        @Override
        public Statistics<K> apply(final Statistics<K> originalValue, final Statistics<K> newValue) {
          originalValue.increaseCounterAndSetLatency(result, latency);
          return originalValue;
        }
      });
    } catch (Exception e) {
      throw new RuntimeException("Exception in measured task " + task.toString(), e);
    }
  }

  public K[] getKeys() {
    return keys;
  }

  public List<Statistics<K>> peekAll() {
    return this.statisticsQueue.peekAll(getTime() - 5000);
  }

  public boolean hasEmptyQueue() {
    return statisticsQueue.size() == 0;
  }
}
