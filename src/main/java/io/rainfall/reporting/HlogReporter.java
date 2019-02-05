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
import io.rainfall.statistics.StatisticsPeekHolder;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;

import java.io.File;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.List;

import static io.rainfall.utils.CompressionUtils.cleanFilename;


/**
 * @author Ludovic Orban
 */
public class HlogReporter<E extends Enum<E>> extends FileReporter<E> {

  private final String basedir;

  public HlogReporter() {
    this("target/rainfall-histograms");
  }

  public HlogReporter(String outputPath) {
    this.basedir = new File(outputPath).getAbsoluteFile().getAbsolutePath();
    this.reportPath = new File(this.basedir);
  }

  @Override
  public void header(final List<String> description) {
  }

  @Override
  public void report(final StatisticsPeekHolder<E> statisticsHolder) {
  }

  @Override
  public void summarize(final StatisticsHolder<E> statisticsHolder) {
    // dump raw histograms as hlog files
    long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
    long endTime = System.currentTimeMillis();
    try {
      Enum<E>[] results = statisticsHolder.getResultsReported();
      for (Enum<E> result : results) {
        Histogram rawHistogram = statisticsHolder.fetchHistogram(result);
        rawHistogram.setStartTimeStamp(startTime);
        rawHistogram.setEndTimeStamp(endTime);

        File hlogFile = new File(this.basedir + File.separatorChar + buildHlogFilename(result.name()));
        hlogFile.getParentFile().mkdirs();
        HistogramLogWriter writer = new HistogramLogWriter(new PrintStream(hlogFile));
        writer.setBaseTime(startTime);

        writer.outputLogFormatVersion();
        writer.outputBaseTime(writer.getBaseTime());
        writer.outputLegend();
        writer.outputIntervalHistogram(rawHistogram);

        writer.close();
      }
    } catch (Exception e) {
      throw new RuntimeException("Can not report to hlog", e);
    }
  }

  private String buildHlogFilename(String result) {
    return cleanFilename(result) + ".hlog";
  }

  @Override
  public String toString() {
    return "Hlog reporter (recording to " + this.reportPath + ")";
  }
}
