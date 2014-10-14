package org.rainfall.statistics;

import jsr166e.ConcurrentHashMapV8;

import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver<K extends Enum<K>> {

  private final K[] keys;
  private final ConcurrentHashMapV8<Long, Statistics<K>> statisticsMap = new ConcurrentHashMapV8<Long, Statistics<K>>();
  private final PriorityQueue<Statistics<K>> statisticsQueue = new PriorityQueue<Statistics<K>>();

  public StatisticsObserver(final Class<K> results) {
    this.keys = results.getEnumConstants();
  }

  protected long getTime() {
    return System.currentTimeMillis();
  }

  public void measure(Task<K> task) {
    try {
      final long start = getTime();
      final K result = task.definition();
      final long end = getTime();
      final long latency = (end - start);
      long timestamp = start;
      this.statisticsMap.merge(timestamp,
          new Statistics<K>(keys, result, latency), new ConcurrentHashMapV8.BiFun<Statistics<K>, Statistics<K>, Statistics<K>>() {
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

  public ConcurrentHashMapV8<Long, Statistics<K>> getStatisticsMap() {
    return this.statisticsMap;
  }
}
