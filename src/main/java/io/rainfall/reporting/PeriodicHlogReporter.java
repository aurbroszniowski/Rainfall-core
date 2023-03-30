/*
 * Copyright (c) 2014-2023 Aurélien Broszniowski
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
import io.rainfall.statistics.StatisticsPeekHolder;
import io.rainfall.utils.CompressionUtils;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static io.rainfall.utils.CompressionUtils.CRLF;
import static io.rainfall.utils.CompressionUtils.cleanFilename;


/**
 * @author Ludovic Orban
 */
public class PeriodicHlogReporter<E extends Enum<E>> extends FileReporter<E> {

  private static class Holder {
    Histogram histogram;
    HistogramLogWriter writer;
    long previousTs;
  }

  private final String basedir;
  private String reportFile;
  private long startTs;
  private final ConcurrentHashMap<Enum<?>, Holder> previous = new ConcurrentHashMap<Enum<?>, Holder>();

  private CompressionUtils compressionUtils = new CompressionUtils();

  public PeriodicHlogReporter() {
    this("target/rainfall-histograms");
  }

  public PeriodicHlogReporter(String outputPath) {
    try {
      this.basedir = new File(outputPath).getAbsoluteFile().getAbsolutePath();
      this.reportPath = new File(this.basedir);
      this.reportFile = this.basedir + File.separatorChar + "report.html";

      compressionUtils.deleteDirectory(new File(this.basedir));

      compressionUtils.extractResources("/report", this.basedir);

      compressionUtils.extractReportTemplateToFile("/template/Hdr-template.html", this.reportFile);

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

    this.startTs = System.currentTimeMillis();
  }

  @Override
  public void report(final StatisticsPeekHolder<E> statisticsPeekHolder) {
    long now = System.currentTimeMillis();

    Enum<E>[] results = statisticsPeekHolder.getResultsReported();
    for (Enum<E> result : results) {
      Histogram histogram = statisticsPeekHolder.fetchHistogram(result);
      Histogram copy = histogram.copy();
      histogram.setEndTimeStamp(now);

      Holder previous = this.previous.get(result);
      if (previous == null) {
        try {
          histogram.setStartTimeStamp(statisticsPeekHolder.getStartTime());
          previous = new Holder();
          previous.previousTs = startTs;
          File hlogFile = new File(this.basedir + File.separatorChar + buildHlogFilename(result.name()));
          hlogFile.getParentFile().mkdirs();
          previous.writer = new HistogramLogWriter(new PrintStream(hlogFile));
          previous.writer.setBaseTime(startTs);
          previous.writer.outputLogFormatVersion();
          previous.writer.outputBaseTime(previous.writer.getBaseTime());
          previous.writer.outputLegend();
          this.previous.put(result, previous);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      } else {
        histogram.setStartTimeStamp(previous.previousTs);
        histogram.subtract(previous.histogram);
      }

      previous.histogram = copy;
      previous.writer.outputIntervalHistogram(histogram);
      previous.previousTs = now;
    }
  }

  @Override
  public void summarize(final StatisticsHolder<E> statisticsHolder) {
    for (Holder holder : previous.values()) {
      holder.writer.close();
    }
    try {
      StringBuilder sb = new StringBuilder();
      Enum<E>[] results = statisticsHolder.getResultsReported();
      for (Enum<E> result : results) {
        sb.append("reportTps('").append(result.name()).append("', 'TPS for ").append(result.name())
            .append("');").append(CRLF);
        sb.append("reportResponseTime('")
            .append(result.name())
            .append("', 'Periodic Response Time for ")
            .append(result.name())
            .append("');")
            .append(CRLF);
        sb.append("reportPercentiles('")
            .append(result.name())
            .append("', 'Response Time percentiles for ")
            .append(result.name())
            .append("');")
            .append(CRLF);
      }
      compressionUtils.substituteInFile(reportFile, "//!summary!", sb);
    } catch (Exception e) {
      throw new RuntimeException("Can not report to Html", e);
    }
  }

  private String buildHlogFilename(String result) {
    return cleanFilename(result) + ".hlog";
  }

  @Override
  public String toString() {
    return "Periodic Hlog reporter (recording to " + this.reportPath + ")";
  }
}
