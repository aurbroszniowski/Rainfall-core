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

package io.rainfall.reporting;

import io.rainfall.Reporter;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeek;
import io.rainfall.statistics.StatisticsPeekHolder;
import org.HdrHistogram.Histogram;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;


/**
 * report the statistics to the text console
 *
 * @author Aurelien Broszniowski
 */

public class TextReporter<E extends Enum<E>> extends Reporter<E> {

  private static final String FORMAT = "%-15s %-15s %12s %10s %10s";
  //  private static final String FORMAT = "%-15s %-7s %12s %10s %10s %10s %10s %10s";
  private static final NumberFormat nf = NumberFormat.getInstance();
  private String CRLF = System.getProperty("line.separator");
  private Calendar calendar = GregorianCalendar.getInstance(TimeZone.getDefault());
  private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

  @Override
  public void report(final StatisticsPeekHolder<E> statisticsPeekHolder) {
    StringBuilder sb = new StringBuilder();
    StatisticsPeek<E> totalStatisticsPeeks = statisticsPeekHolder.getTotalStatisticsPeeks();
    Set<String> keys = statisticsPeekHolder.getStatisticsPeeksNames();

    sb.append("===================================================== PERIODIC ==========================================")
        .append(CRLF);
    sb.append(String.format(FORMAT, "Cache", "Type", "Txn_Count", "TPS", "Avg_Lat"))
//    sb.append(String.format(FORMAT, "Cache", "Type", "Txn_Count", "TPS", "Avg_Lat", "Min_Lat", "Max_Lat", "TotalExceptionCount"))
        .append(CRLF);
    sb.append("==========================================================================================================")
        .append(CRLF);

    for (String key : keys) {
      StatisticsPeek<E> statisticsPeeks = statisticsPeekHolder.getStatisticsPeeks(key);
      logPeriodicStats(sb, key, statisticsPeeks, statisticsPeekHolder.getResultsReported());
    }

    if (totalStatisticsPeeks != null)
      logPeriodicStats(sb, "ALL", totalStatisticsPeeks, statisticsPeekHolder.getResultsReported());

    System.out.println(sb.toString());
  }

  @Override
  public void summarize(final StatisticsHolder<E> statisticsHolder) {
//    sb.append(String.format(FORMAT, "Cache", "Type", "Txn_Count", "TPS", "Avg_Lat"))
    Enum<E>[] results = statisticsHolder.getResultsReported();
    for (Enum<E> result : results) {
      System.out.println("Percentiles distribution for result : " + result);
      Histogram histogram = statisticsHolder.getHistogram(result);
      histogram.outputPercentileDistribution(System.out, 5, 1000000d, false);
    }
  }

  private void logPeriodicStats(StringBuilder sb, String name, StatisticsPeek<E> peek, final Enum<E>[] resultsReported) {
    sb.append(formatTimestampInMs(peek.getTimestamp())).append(CRLF);

    for (Enum<E> result : resultsReported) {
      sb.append(String.format(FORMAT,
          name,
          result.name(),
          nf.format(peek.getPeriodicCounters(result)),
          nf.format(peek.getPeriodicTps(result)),
          nf.format(peek.getPeriodicAverageLatencyInMs(result))
      )).append(CRLF);
    }
    sb.append(String.format(FORMAT,
        name,
        "TOTAL",
        nf.format(peek.getSumOfPeriodicCounters()),
        nf.format(peek.getSumOfPeriodicTps()),
        nf.format(peek.getAverageOfPeriodicAverageLatencies())
    )).append(CRLF);
  }

  private String formatTimestampInMs(final long timestamp) {
    calendar.setTime(new Date(timestamp));
    return sdf.format(calendar.getTime());
  }
}
