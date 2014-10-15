package org.rainfall.statistics;

import jsr166e.ConcurrentHashMapV8;

/**
 * A {@link org.rainfall.statistics.Statistics} instance holds the statistics of all results at a given point in time
 *
 * @author Aurelien Broszniowski
 */

public class Statistics<K extends Enum<K>> {

  private final K[] keys;
  private final Long timestamp;
  private final ConcurrentHashMapV8<K, Long> counters = new ConcurrentHashMapV8<K, Long>();
  private final ConcurrentHashMapV8<K, Long> latencies = new ConcurrentHashMapV8<K, Long>();

  public Statistics(K[] keys, final Long timestamp, final K result, final Long latency) {
    this.keys = keys;
    this.timestamp = timestamp;
    for (K key : keys) {
      this.counters.put(key, new Long(0));
      this.latencies.put(key, new Long(0));
    }
    this.counters.put(result, new Long(1));
    this.latencies.put(result, latency);
  }

  public void increaseCounterAndSetLatency(final K result, Long latency) {
    //TODO improve the atomicity
    synchronized (counters.get(result)) {
      long cnt = this.counters.get(result);
      long updatedLatency = (this.latencies.get(result) * cnt + latency) / (cnt + 1);
      this.latencies.put(result, updatedLatency);  //TODO : use merge
      this.counters.put(result, ++cnt);          //TODO use merge
    }
  }

  public K[] getKeys() {
    return keys;
  }

  public ConcurrentHashMapV8<K, Long> getCounter() {
    return counters;
  }

  public ConcurrentHashMapV8<K, Long> getLatency() {
    return latencies;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public Long sumOfCounters() {
    Long total = 0L;
    synchronized (counters) {
      for (Enum<K> key : keys) {
        total += counters.get(key);
      }
    }
    return total;
  }

  public Double averageLatencyInMs() {
    Double average = 0.0d;
    synchronized (latencies) {
      for (Enum<K> key : keys) {
        average += latencies.get(key);
      }
      average /= keys.length;
    }
    return average / 1000000L;
  }
}