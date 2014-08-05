package org.rainfall.reporting;

import org.rainfall.Reporter;
import org.rainfall.statistics.StatisticsObserver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * report the statistics to the text console
 *
 * @author Aurelien Broszniowski
 */

public class TextReporter<K extends Enum<K>> implements Reporter {

  @Override
  public void report(final StatisticsObserver statisticsObserver) {

    //TODO : fix Generics unchecked call
    Map<K, AtomicLong> counters = statisticsObserver.getCounter();
    Set<K> keys = counters.keySet();
    StringBuilder sb = new StringBuilder();
    sb.append("KEY \t counter \t minLatency \t maxLatency \t averageLatency \n");
    synchronized (counters) {
      for (K key : keys) {
        Long minLatency = (Long)statisticsObserver.getMinLatency().get(key);
        Long maxLatency = (Long)statisticsObserver.getMaxLatency().get(key);
        sb.append(key).append(" \t\t ")
            .append(counters.get(key)).append(" \t\t ")
            .append(minLatency == Long.MAX_VALUE ? "NA" : minLatency).append(" \t\t ")
            .append(maxLatency == Long.MIN_VALUE ? "NA" : maxLatency).append(" \t\t ")
            .append(statisticsObserver.getAverageLatency().get(key))
            .append(" \n");
      }
    }
    System.out.println(sb.toString());
  }

}
