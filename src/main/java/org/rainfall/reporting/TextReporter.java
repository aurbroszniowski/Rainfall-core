/*
 * Copyright 2014 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rainfall.reporting;

import org.rainfall.Reporter;
import org.rainfall.statistics.Result;
import org.rainfall.statistics.Statistics;
import org.rainfall.statistics.StatisticsHolder;
import org.rainfall.statistics.StatisticsObserver;
import org.rainfall.statistics.StatisticsObserversFactory;

import java.text.NumberFormat;
import java.util.Set;


/**
 * report the statistics to the text console
 *
 * @author Aurelien Broszniowski
 */

public class TextReporter implements Reporter {

  private static final String FORMAT = "%-15s %-15s %12s %10s %10s";
  //  private static final String FORMAT = "%-15s %-7s %12s %10s %10s %10s %10s %10s";
  private static final NumberFormat nf = NumberFormat.getInstance();
  private String CRLF = System.getProperty("line.separator");

  @Override
  public void report(final StatisticsObserversFactory observersFactory) {
    StringBuilder sb = new StringBuilder();
    sb.append("==================================================== CUMULATIVE =========================================")
        .append(CRLF);
    sb.append(String.format(FORMAT, "Cache", "Type", "Txn_Count", "TPS", "Avg_Lat"))
//    sb.append(String.format(FORMAT, "Cache", "Type", "Txn_Count", "TPS", "Avg_Lat", "Min_Lat", "Max_Lat", "TotalExceptionCount"))
        .append(CRLF);
    sb.append("==========================================================================================================")
        .append(CRLF);

    Set<String> statKeys = observersFactory.getStatisticObserverKeys();
    for (String statKey : statKeys) {
      StatisticsObserver observer = observersFactory.getStatisticObserver(statKey);

      StatisticsHolder holder = observer.peek();
      logStats(sb, statKey, holder);
    }
    StatisticsObserver totalStatisticObserver = observersFactory.getTotalStatisticObserver();
    if (totalStatisticObserver != null)
      logStats(sb, "ALL", totalStatisticObserver.peek());

/*
    Long timestamp = statisticsObserver.getTimestamp();
    sb.append(timestamp).append(" \t\t ");
    sb.append("KEY \t counter \t minLatency \t maxLatency \t averageLatencyInMs ");
    sb.append(System.getProperty("line.separator"));
    Statistics statistics = statisticsObserver.getStatistics();
    sb.append("Total operations: ").append(String.format("%,8d", statistics.sumOfCounters())).append(" ops \t");
    sb.append("Average Latency: ").append(String.format("%.2f", statistics.averageLatencyInMs())).append("ms \t");
    sb.append("Average TPS: ").append(String.format("%,8d", statistics.averageTps()));
    sb.append(System.getProperty("line.separator"));
    Result[] results = statistics.getKeys();
    for (Result result : results) {
      sb.append(result.value()).append(" \t\t ");
      sb.append("Number of operations: ")
          .append(String.format("%,8d", statistics.getCounter(result)))
          .append(" ops \t");
      sb.append("Average Latency: ").append(String.format("%.2f", statistics.getLatency(result))).append("ms \t");
      sb.append("TPS: ").append(String.format("%,8d", statistics.getTps(result)));

      sb.append(CRLF);
    }
    sb.append("--------------------------------------------------------------------------------------------");
    sb.append(System.getProperty("line.separator"));*/
    System.out.println(sb.toString());
  }

  private void logStats(StringBuilder sb, String name, StatisticsHolder holder) {
    sb.append(format(holder.getTimestamp())).append(CRLF);
    Statistics statistics = holder.getStatistics();
    Result[] keys = statistics.getKeys();
    for (Result key : keys) {
      sb.append(String.format(FORMAT,
          name,
          key.value(),
          nf.format(statistics.getCounter(key)),
          nf.format(statistics.getTps(key)),
          nf.format(statistics.getLatency(key))
      )).append(CRLF);
    }
    sb.append(String.format(FORMAT,
        name,
        "TOTAL",
        nf.format(statistics.sumOfCounters()),
        nf.format(statistics.averageTps()),
        nf.format(statistics.averageLatencyInMs())
    )).append(CRLF);
  }

  private String format(final long timestamp) {
    long timeInSec = timestamp / (1000 * 1000000L);
    long second = timeInSec % 60;
    long minute = (timeInSec / 60) % 60;
    long hour = (timeInSec / 60 * 60) % 24;

    return String.format("%02d:%02d:%02d", hour, minute, second);
  }

}
