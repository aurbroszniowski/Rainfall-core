package org.rainfall.reporting;

import org.rainfall.Reporter;
import org.rainfall.statistics.StatisticsObserver;

import java.util.Set;

/**
 * report the statistics to the text console
 *
 * @author Aurelien Broszniowski
 */

public class TextReporter implements Reporter {

  @Override
  public void report(StatisticsObserver statisticsObserver) {

    Set keys = statisticsObserver.getCounter().keySet();
    StringBuilder sb = new StringBuilder();
    sb.append("KEY \t counter \t minLatency \t maxLatency \t averageLatency \n");
    //TODO add write lock instead - read is permitted ? or improve concurrency on another way
    synchronized (statisticsObserver.getCounter()) {
      for (Object key : keys) {
        Long minLatency = (Long)statisticsObserver.getMinLatency().get(key);
        Long maxLatency = (Long)statisticsObserver.getMaxLatency().get(key);
        sb.append(key).append(" \t\t ")
            .append(statisticsObserver.getCounter().get(key)).append(" \t\t ")
            .append(minLatency == Long.MAX_VALUE ? "NA" : minLatency).append(" \t\t ")
            .append(maxLatency == Long.MIN_VALUE ? "NA" : maxLatency).append(" \t\t ")
            .append(statisticsObserver.getAverageLatency().get(key))
            .append(" \n");
      }
    }
    System.out.println(sb.toString());
  }

}
