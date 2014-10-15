package org.rainfall.reporting;

import org.rainfall.Reporter;
import org.rainfall.statistics.Statistics;

import java.util.List;

/**
 * report the statistics to the text console
 *
 * @author Aurelien Broszniowski
 */

public class TextReporter<K extends Enum<K>> implements Reporter<K> {

  @Override
  public void report(final List<Statistics<K>> statistics) {
    for (Statistics<K> next : statistics) {
      StringBuilder sb = new StringBuilder();
      sb.append("KEY \t counter \t minLatency \t maxLatency \t averageLatencyInMs \n");
      Long timestamp = next.getTimestamp();
      //TODO add calculation of total nb of ops / average latency
      System.out.println("** " + (timestamp));
      sb.append(timestamp).append(" \t\t ");
      sb.append("Total operations: ").append(next.sumOfCounters()).append(" ops");
      sb.append("Average Latency : ").append(next.averageLatencyInMs()).append("ms");
      sb.append(System.getProperty("line.separator"));
      K[] results = next.getKeys();
      for (K result : results) {
        sb.append(timestamp).append(" \t\t ");
        sb.append("Number of operations: ").append(next.getCounter().get(result)).append(" ops");
        sb.append("Average Latency : ").append(next.getLatency().get(result)).append("ms");
        sb.append(System.getProperty("line.separator"));
      }
      sb.append("--------------------------------------------------------------------------------------------");
      sb.append(System.getProperty("line.separator"));
      System.out.println(sb.toString());
    }
  }

}
