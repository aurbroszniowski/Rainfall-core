package org.rainfall.statistics;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver<K extends Enum<K>> {

  private final Map<K, AtomicLong> counter;
  private final Map<K, Long> minLatency;
  private final Map<K, Long> maxLatency;
  private final Map<K, Double> averageLatency;

  public StatisticsObserver(final Class<K> results) {
    this.counter = new EnumMap<K, AtomicLong>(results);
    this.minLatency = Collections.synchronizedMap(new EnumMap<K, Long>(results));
    this.maxLatency = Collections.synchronizedMap(new EnumMap<K, Long>(results));
    this.averageLatency = Collections.synchronizedMap(new EnumMap<K, Double>(results));

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

  public Map<K, AtomicLong> getCounter() {
    return counter;
  }

  public Map<K, Long> getMinLatency() {
    return minLatency;
  }

  public Map<K, Long> getMaxLatency() {
    return maxLatency;
  }

  public Map<K, Double> getAverageLatency() {
    return averageLatency;
  }
}
