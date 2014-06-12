package org.rainfall.statistics;

import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver<T extends Enum<T>> {

  private final EnumMap<T, AtomicLong> counter;
  private final EnumMap<T, Long> minLatency;
  private final EnumMap<T, Long> maxLatency;
  private final EnumMap<T, Double> averageLatency;

  public StatisticsObserver(final Class<T> results) {
    this.counter = new EnumMap<T, AtomicLong>(results);
    this.minLatency = new EnumMap<T, Long>(results);
    this.maxLatency = new EnumMap<T, Long>(results);
    this.averageLatency = new EnumMap<T, Double>(results);

    for (T t : results.getEnumConstants()) {
      this.counter.put(t, new AtomicLong(0));
      this.minLatency.put(t, Long.MAX_VALUE);
      this.maxLatency.put(t, Long.MIN_VALUE);
      this.averageLatency.put(t, 0.0);
    }
  }

  public long start() {
    return System.nanoTime();
  }

  public void end(final long start, final T result) {
    long end = System.nanoTime();
    long latency = (end - start) / 1000000;

    long nbResults = counter.get(result).getAndIncrement();
    System.out.println("+++++++ >> " + nbResults);

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

  @Override
  public String toString() {
    Set<T> keys = counter.keySet();
    StringBuilder sb = new StringBuilder();
    sb.append(" KEY \t counter \t minLatency \t maxLatency \t averageLatency \n");
    for (T key : keys) {
      Long minLatency = this.minLatency.get(key);
      Long maxLatency = this.maxLatency.get(key);
      sb.append(key).append(" \t ")
          .append(counter.get(key)).append(" \t ")
          .append(minLatency == Long.MAX_VALUE ? "NA" : minLatency).append(" \t ")
          .append(maxLatency == Long.MIN_VALUE ? "NA" : maxLatency).append(" \t ")
          .append(averageLatency.get(key))
          .append(" \n");
    }
    return sb.toString();
  }
}
