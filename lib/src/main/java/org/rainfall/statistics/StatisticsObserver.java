package org.rainfall.statistics;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver<K extends Enum<K>> {

  private final EnumMap<K, AtomicLong> counter;
  private final EnumMap<K, Long> minLatency;
  private final EnumMap<K, Long> maxLatency;
  private final EnumMap<K, Double> averageLatency;

  public StatisticsObserver(final Class<K> results) {
    this.counter = new EnumMap<K, AtomicLong>(results);
    this.minLatency = new EnumMap<K, Long>(results);
    this.maxLatency = new EnumMap<K, Long>(results);
    this.averageLatency = new EnumMap<K, Double>(results);

    for (K t : results.getEnumConstants()) {
      this.counter.put(t, new AtomicLong(0));
      this.minLatency.put(t, Long.MAX_VALUE);
      this.maxLatency.put(t, Long.MIN_VALUE);
      this.averageLatency.put(t, 0.0);
    }
  }

  public long start() {
    return System.nanoTime();
  }

  public void end(final long start, final K result) {
    long end = System.nanoTime();
    long latency = (end - start) / 1000000;

    long nbResults = counter.get(result).getAndIncrement();

    synchronized (minLatency) {
      if (this.minLatency.get(result) > latency) {
        this.minLatency.put(result, latency);
      }
    }

    synchronized (maxLatency) {
      if (this.maxLatency.get(result) < latency) {
        this.maxLatency.put(result, latency);
      }
    }

    synchronized (averageLatency) {
      double average = ((this.averageLatency.get(result) * nbResults) + latency) / (nbResults + 1);
      this.averageLatency.put(result, average);
    }
  }

  public Long getSumOfCounters() {
    AtomicLong sum = new AtomicLong();
    for (AtomicLong cnt : counter.values()) {
      sum.addAndGet(cnt.longValue());
    }
    return sum.longValue();
  }

  public EnumMap<K, AtomicLong> getCounter() {
    return counter;
  }

  public EnumMap<K, Long> getMinLatency() {
    return minLatency;
  }

  public EnumMap<K, Long> getMaxLatency() {
    return maxLatency;
  }

  public EnumMap<K, Double> getAverageLatency() {
    return averageLatency;
  }
}
