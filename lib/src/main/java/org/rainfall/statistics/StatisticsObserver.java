package org.rainfall.statistics;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver<K extends Enum<K>> {

  private final K[] keys;

  private final Map<K, Long> timestamps;
  private final Map<K, AtomicLong> counter;
  private final Map<K, Long> minLatency;
  private final Map<K, Long> maxLatency;
  private final Map<K, Double> averageLatency;

  public StatisticsObserver(final Class<K> results) {
    this.keys = results.getEnumConstants();

    this.timestamps = Collections.synchronizedMap(new EnumMap<K, Long>(results));
    this.counter = new EnumMap<K, AtomicLong>(results);
    this.minLatency = Collections.synchronizedMap(new EnumMap<K, Long>(results));
    this.maxLatency = Collections.synchronizedMap(new EnumMap<K, Long>(results));
    this.averageLatency = Collections.synchronizedMap(new EnumMap<K, Double>(results));

    for (K k : keys) {
      this.timestamps.put(k, System.nanoTime());
      this.counter.put(k, new AtomicLong(0));
      this.minLatency.put(k, Long.MAX_VALUE);
      this.maxLatency.put(k, Long.MIN_VALUE);
      this.averageLatency.put(k, 0.0);
    }
  }

  public long start() {
    return System.nanoTime();
  }

  public void end(final long start, final K result) {
    long end = System.nanoTime();
    long latency = (end - start) / 1000000;

    this.timestamps.put(result, start );

    long nbResults = counter.get(result).getAndIncrement();

    if (this.minLatency.get(result) > latency) {
      this.minLatency.put(result, latency);
    }

    if (this.maxLatency.get(result) < latency) {
      this.maxLatency.put(result, latency);
    }

    double average = ((this.averageLatency.get(result) * nbResults) + latency) / (nbResults + 1);
    this.averageLatency.put(result, average);
  }

  public Long getSumOfCounters() {
    AtomicLong sum = new AtomicLong();
    for (AtomicLong cnt : counter.values()) {
      sum.addAndGet(cnt.longValue());
    }
    return sum.longValue();
  }

  public K[] getKeys() {
    return keys;
  }

  public Long getTimestamps(K key) {
    return timestamps.get(key);
  }

  public Long getTimestamp() {
    return timestamps.get(keys[0]);
  }

  public AtomicLong getCounter(K key) {
    return counter.get(key);
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

}
