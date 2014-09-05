package org.rainfall.reporting;

import org.rainfall.Reporter;
import org.rainfall.statistics.StatisticsObserver;

import java.util.Set;

/**
 * report the statistics to the text console
 *
 * @author Aurelien Broszniowski
 */

public class TextReporter<K extends Enum<K>> implements Reporter {

  @Override
  public void report(StatisticsObserver statisticsObserver) {
    StringBuilder sb = new StringBuilder();
    sb.append("KEY \t counter \t minLatency \t maxLatency \t averageLatency \n");
    Enum<K>[] keys = statisticsObserver.getKeys();

    //TODO add write lock instead - read is permitted ? or improve concurrency on another way
    synchronized (statisticsObserver.getKeys()) {
      for (Enum<K> key : keys) {
        Long minLatency = statisticsObserver.getMinLatency(key);
        Long maxLatency = statisticsObserver.getMaxLatency(key);

        sb.append(key).append(" \t\t ")
            .append(statisticsObserver.getCounter(key)).append(" \t\t ")
            .append(minLatency == Long.MAX_VALUE ? "NA" : minLatency).append(" \t\t ")
            .append(maxLatency == Long.MIN_VALUE ? "NA" : maxLatency).append(" \t\t ")
            .append(statisticsObserver.getAverageLatency(key))
            .append(" \n");
      }
    }
    System.out.println(sb.toString());
  }

}
