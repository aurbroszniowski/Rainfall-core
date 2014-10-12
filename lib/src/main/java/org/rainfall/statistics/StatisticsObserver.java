package org.rainfall.statistics;

import jsr166e.ConcurrentHashMapV8;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver<K extends Enum<K>> {

  private final K[] keys;
  private final ConcurrentHashMapV8<K, Long> minLatency;
  private final ConcurrentHashMapV8<K, Long> maxLatency;
  private final ConcurrentHashMapV8<K, Double> averageLatency;
  private final AtomicLong nbStatsEvents = new AtomicLong(0);

  private final ConcurrentHashMapV8<Long, Statistics<K>> statisticsMap = new ConcurrentHashMapV8<Long, Statistics<K>>();

  public StatisticsObserver(final Class<K> results) {
    this.keys = results.getEnumConstants();

    this.minLatency = new ConcurrentHashMapV8<K, Long>();
    this.maxLatency = new ConcurrentHashMapV8<K, Long>();
    this.averageLatency = new ConcurrentHashMapV8<K, Double>();

    for (K k : keys) {
      this.minLatency.put(k, Long.MAX_VALUE);
      this.maxLatency.put(k, Long.MIN_VALUE);
      this.averageLatency.put(k, 0.0);
    }
  }

  protected long getTime() {
    return System.nanoTime();
  }

  public long start() {
    return getTime();
  }

  public void end(final long start, final K result) {
    long end = getTime();
    final long latency = (end - start) / 1000000;

    this.averageLatency.merge(result, (double)latency, new ConcurrentHashMapV8.BiFun<Double, Double, Double>() {
      @Override
      public Double apply(final Double originalValue, final Double newValue) {
        synchronized (nbStatsEvents) {
          long counter = nbStatsEvents.getAndIncrement();
          return (originalValue * counter + newValue) / (counter + 1);
        }
      }
    });

    this.minLatency.merge(result, latency, new ConcurrentHashMapV8.BiFun<Long, Long, Long>() {
      @Override
      public Long apply(final Long originalValue, final Long newValue) {
        return newValue < originalValue ? newValue : originalValue;
      }
    });

    this.maxLatency.merge(result, latency, new ConcurrentHashMapV8.BiFun<Long, Long, Long>() {
      @Override
      public Long apply(final Long originalValue, final Long newValue) {
        return newValue > originalValue ? newValue : originalValue;
      }
    });

    this.statisticsMap.merge(start, new Statistics<K>(keys, latency), new ConcurrentHashMapV8.BiFun<Statistics<K>, Statistics<K>, Statistics<K>>() {
      @Override
      public Statistics<K> apply(final Statistics<K> originalValue, final Statistics<K> newValue) {
        originalValue.increaseCounterAndSetLatency(result, latency);
        return originalValue;
      }
    });
  }

  public K[] getKeys() {
    return keys;
  }

  public Long getMinLatency(K key) {
    return minLatency.get(key);
  }

  public Long getMaxLatency(K key) {
    return maxLatency.get(key);
  }

  public Double getAverageLatency(K key) {
    return averageLatency.get(key);
  }

  public ConcurrentHashMapV8<Long, Statistics<K>> getStatisticsMap() {
    return statisticsMap;
  }
}
