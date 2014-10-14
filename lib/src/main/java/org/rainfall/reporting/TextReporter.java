package org.rainfall.reporting;

import jsr166e.ConcurrentHashMapV8;
import org.rainfall.Reporter;
import org.rainfall.statistics.Statistics;
import org.rainfall.statistics.StatisticsObserver;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
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

    //TODO consider using a queue instead of a map, so it gets ordered operations
    //TODO add calculation of total nb of ops / average latency
    ConcurrentHashMapV8<Long, Statistics<K>> statisticsMap = statisticsObserver.getStatisticsMap();
    Iterator<Map.Entry<Long,Statistics<K>>> iterator = statisticsMap.entrySet().iterator();
    if (!iterator.hasNext())
      return;
    Map.Entry<Long, Statistics<K>> next = iterator.next();

    Long timestamp = next.getKey();
    sb.append(timestamp).append(" \t\t ");
    sb.append("Total operations: ").append(next.getValue().sumOfCounters()).append(" ops");
    sb.append("Average Latency : ").append(next.getValue().averageLatency()).append("ms");
    sb.append(System.getProperty("line.separator"));
     K[] results = next.getValue().getKeys();
    for (K result : results) {
      sb.append(timestamp).append(" \t\t ");
      sb.append("Number of operations: ").append(next.getValue().getCounter().get(result)).append(" ops");
      sb.append("Average Latency : ").append(next.getValue().getLatency().get(result)).append("ms");
      sb.append(System.getProperty("line.separator"));
    }
    sb.append("--------------------------------------------------------------------------------------------");
    sb.append(System.getProperty("line.separator"));
    statisticsMap.remove(timestamp);

    System.out.println(sb.toString());
  }

}
