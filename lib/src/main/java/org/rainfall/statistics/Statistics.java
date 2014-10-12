package org.rainfall.statistics;

import jsr166e.ConcurrentHashMapV8;

/**
 * A {@link org.rainfall.statistics.Statistics} instance holds the statistics of all results at a given point in time
 *
 * @author Aurelien Broszniowski
 */

public class Statistics<K extends Enum<K>> {

  private final K[] keys;
  private final ConcurrentHashMapV8<K, Long> counter = new ConcurrentHashMapV8<K, Long>();
  private final ConcurrentHashMapV8<K, Long> latency = new ConcurrentHashMapV8<K, Long>();

  public Statistics(K[] keys, final Long latency) {
    this.keys = keys;
  }

  public void increaseCounterAndSetLatency(final K result, Long latency) {
    //TODO refactor to have an atomic operation
    synchronized (counter.get(result)) {
      long cnt = this.counter.putIfAbsent(result, 0L);
      long updatedLatency = (this.latency.putIfAbsent(result, 0L) * cnt + latency) / (cnt + 1);
      this.latency.put(result, updatedLatency);  //TODO : use merge
      this.counter.put(result, ++cnt);          //TODO use merge
    }
  }

  public Long sumOfCounters() {
    Long total = 0L;
    synchronized (counter) {
      for (K key : keys) {
        total += counter.get(key);
      }
    }
    return total;
  }

  public Double averageLatency() {
    Double average = 0.0d;
    synchronized (latency) {
      for (K key : keys) {
        average += latency.get(key);
      }
      average /= keys.length;
    }
    return average;
  }
}