/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
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

import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeek;
import io.rainfall.statistics.StatisticsPeekHolder;
import io.rainfall.statistics.collector.StatisticsCollector;
import io.rainfall.statistics.exporter.Exporter;
import io.rainfall.statistics.exporter.HtmlExporter;
import io.rainfall.utils.CompressionUtils;
import org.HdrHistogram.Histogram;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static io.rainfall.utils.CompressionUtils.CRLF;
import static io.rainfall.utils.CompressionUtils.cleanFilename;


/**
 * @author Aurelien Broszniowski
 */

public class HtmlReporter<E extends Enum<E>> extends FileReporter<E> {

  private String basedir;
  private String averageLatencyFile = "averageLatency.csv";
  private String tpsFile = "tps.csv";
  private String percentilesFile = "total-percentiles.csv";
  private String reportFile;
  private final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
  private Calendar calendar = GregorianCalendar.getInstance(TimeZone.getDefault());
  private SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
  private CompressionUtils compressionUtils = new CompressionUtils();

  public HtmlReporter() {
    this("target/rainfall-report");
  }

  public HtmlReporter(String outputPath) {
    try {
      this.basedir = new File(outputPath).getAbsoluteFile().getAbsolutePath();
      this.reportPath = new File(this.basedir);
      this.reportFile = this.basedir + File.separatorChar + "report.html";

      compressionUtils.deleteDirectory(new File(this.basedir));

      compressionUtils.extractResources("/report", this.basedir);

      compressionUtils.extractReportTemplateToFile("/template/Tps-template.html", new File(this.reportFile));

    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not read report template");
    } catch (IOException e) {
      throw new RuntimeException("Can not copy report template");
    }
  }

  @Override
  public void header(final List<String> description) {
    StringBuilder sb = new StringBuilder();
    for (String desc : description) {
      sb.append(desc).append("</br>");
    }
    try {
      compressionUtils.substituteInFile(reportFile, "//!header!", sb);
    } catch (Exception e) {
      throw new RuntimeException("Can not report to Html", e);
    }
  }

  @Override
  public void report(final StatisticsPeekHolder<E> statisticsPeekHolder) {
    try {
      StatisticsPeek<E> totalStatisticsPeeks = statisticsPeekHolder.getTotalStatisticsPeeks();

      Set<String> keys = statisticsPeekHolder.getStatisticsPeeksNames();

      for (String key : keys) {
        StatisticsPeek<E> statisticsPeeks = statisticsPeekHolder.getStatisticsPeeks(key);
        logPeriodicStats(key, statisticsPeeks, statisticsPeekHolder.getResultsReported());
      }

      if (totalStatisticsPeeks != null)
        logPeriodicStats("total", totalStatisticsPeeks, statisticsPeekHolder.getResultsReported());

      logPeriodicExtraStats(statisticsPeekHolder.getExtraCollectedStatistics());

    } catch (Exception e) {
      throw new RuntimeException("Can not write report data", e);
    }
  }

  private void logPeriodicExtraStats(final Map<String, Exporter> extraCollectedStatistics) throws Exception {
    for (String statisticsCollectorName : extraCollectedStatistics.keySet()) {
      ((HtmlExporter)extraCollectedStatistics.get(statisticsCollectorName)).ouputCsv(this.basedir);
    }
  }

  @Override
  public void summarize(final StatisticsHolder<E> statisticsHolder) {
    try {
      copyReportTemplate(statisticsHolder);
      StringBuilder sb = new StringBuilder();
      Enum<E>[] results = statisticsHolder.getResultsReported();

      for (Enum<E> result : results) {
        Histogram histogram = statisticsHolder.fetchHistogram(result);
        try {
          histogram = histogram.copyCorrectedForCoordinatedOmission(1000000L);
        } catch (Throwable t) {
          // eat it. Sometimes, some places, it throws an exception here.
        }
        String percentilesFilename = this.basedir + File.separatorChar + getPercentilesFilename(result.name());
        PrintStream stream = new PrintStream(new File(percentilesFilename));
        try {
          histogram.outputPercentileDistribution(stream, 5, 1000000d, true);
        } catch (Exception e) {
          e.printStackTrace();
        }
        stream.close();

        String mean = "NaN";
        try {
          mean = "" + histogram.getMean();
        } catch (Exception e) {
          e.printStackTrace();
        }

        String maxValue = "NaN";
        try {
          maxValue = "" + histogram.getMaxValue();
        } catch (Exception e) {
          e.printStackTrace();
        }

        sb.append("reportPercentiles('")
            .append(getPercentilesFilename(result.name()).substring(0, getPercentilesFilename(result.name()).length() - 4))
            .append("', 'Response Time percentiles for ").append(result.name())
            .append("', '" + mean + "', '" + maxValue)
            .append("');").append(CRLF);
      }
      compressionUtils.substituteInFile(reportFile, "//!summary!", sb);

    } catch (Exception e) {
      throw new RuntimeException("Can not report to Html", e);
    }
    //TODO :  put onglets
  }

  private void logPeriodicStats(String name, StatisticsPeek<E> statisticsPeek, final Enum<E>[] resultsReported) throws IOException {
    String avgFilename = this.basedir + File.separatorChar + getAverageLatencyFilename(name);
    String tpsFilename = this.basedir + File.separatorChar + getTpsFilename(name);

    Writer averageLatencyOutput;
    Writer tpsOutput;

    averageLatencyOutput = new BufferedWriter(new FileWriter(avgFilename, true));
    if (new File(avgFilename).length() == 0)
      addHeader(averageLatencyOutput, resultsReported);
    tpsOutput = new BufferedWriter(new FileWriter(tpsFilename, true));
    if (new File(tpsFilename).length() == 0)
      addHeader(tpsOutput, resultsReported);

    String timestamp = formatTimestampInNano(statisticsPeek.getTimestamp());

    StringBuilder averageLatencySb = new StringBuilder(timestamp);
    StringBuilder tpsSb = new StringBuilder(timestamp);

    for (Enum<E> result : resultsReported) {
      averageLatencySb.append(",")
          .append(String.format("%.4f", (statisticsPeek.getPeriodicAverageLatencyInMs(result))));
      tpsSb.append(",").append(statisticsPeek.getPeriodicTps(result));
    }
    averageLatencyOutput.append(averageLatencySb.toString()).append("\n");
    tpsOutput.append(tpsSb.toString()).append("\n");

    averageLatencyOutput.close();
    tpsOutput.close();
  }

  private String getTpsFilename(String key) {
    return cleanFilename(key) + "-" + this.tpsFile;
  }

  private String getAverageLatencyFilename(String key) {
    return cleanFilename(key) + "-" + this.averageLatencyFile;
  }

  private String getPercentilesFilename(String result) {
    return cleanFilename(result) + "-" + this.percentilesFile;
  }

  /**
   * Define tps/latencies graph in html
   * TODO : We know what is reported after hand (what domains are reported, e.g. cache1, cache2)
   * TODO : We should define this during the constructor, we should be able to know what domain is going to be reported
   */
  private void copyReportTemplate(final StatisticsHolder<E> peek) throws IOException, URISyntaxException {
    Set<String> names = peek.getStatisticsKeys();
    StringBuilder sb = new StringBuilder();
    // Periodic
    for (String name : names) {
      String tpsFilename = getTpsFilename(name);
      sb.append("reportTps('").append(tpsFilename.substring(0, tpsFilename.length() - 4))
          .append("', 'Periodic TPS - ").append(name)
          .append("');").append(CRLF);
    }
    sb.append("reportTps('total-tps', 'Periodic Total TPS');").append(CRLF);
    for (String key : names) {
      String averageLatencyFilename = getAverageLatencyFilename(key);
      sb.append("reportResponseTime('")
          .append(averageLatencyFilename.substring(0, averageLatencyFilename.length() - 4))
          .append("', 'Periodic Response Time - ").append(key)
          .append("');").append(CRLF);
    }
    sb.append("reportResponseTime('total-averageLatency', 'Periodic Average Response Time of all entities');")
        .append(CRLF);
    for (StatisticsCollector statisticsCollector : peek.getStatisticsCollectors()) {
      sb.append(((HtmlExporter)statisticsCollector.peek()).outputHtml()).append(CRLF);
    }


    compressionUtils.substituteInFile(reportFile, "//!report!", sb);
  }

  public String formatTimestampInNano(final long timestamp) {
    calendar.setTime(new Date(timestamp));
    return sdf.format(calendar.getTime());
  }

  public void addHeader(Writer output, Enum[] keys) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("timestamp");
    for (Enum key : keys) {
      sb.append(",").append(key.name());
    }
    output.append(sb.toString()).append("\n");
  }

  @Override
  public String toString() {
    return "Html reporter (recording to " + this.reportPath + ")";
  }
}
