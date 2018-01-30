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

import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeekHolder;
import io.rainfall.utils.CompressionUtils;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Ludovic Orban
 */
public class PeriodicHlogReporter<E extends Enum<E>> extends FileReporter<E> {

  private static class Holder {
    Histogram histogram;
    HistogramLogWriter writer;
  }

  private final String basedir;
  private String reportFile;
  private long previousTs;
  private final ConcurrentHashMap<Enum<?>, Holder> previous = new ConcurrentHashMap<Enum<?>, Holder>();
  private final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
  private CompressionUtils compressionUtils = new CompressionUtils();
  private final static String CRLF = System.getProperty("line.separator");

  public PeriodicHlogReporter() {
    this("target/rainfall-histograms");
  }

  public PeriodicHlogReporter(String outputPath) {
    try {
      this.basedir = new File(outputPath).getAbsoluteFile().getAbsolutePath();
      this.reportPath = new File(this.basedir);
      this.reportFile = this.basedir + File.separatorChar + "report.html";

      deleteDirectory(new File(this.basedir));

      if (jarFile.isFile()) {  // Run with JAR file
        compressionUtils.extractFromJar("/report", this.basedir);
      } else {
        compressionUtils.extractFromPath(new File(HtmlReporter.class.getClass()
            .getResource("/report")
            .toURI()), new File(this.basedir));
      }

      extractReportFile();

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
      substituteInFile(reportFile, "//!header!", sb);
    } catch (Exception e) {
      throw new RuntimeException("Can not report to Html", e);
    }

    this.previousTs = System.currentTimeMillis();
  }

  @Override
  public void report(final StatisticsPeekHolder<E> statisticsHolder) {
    long now = System.currentTimeMillis();

    Enum<E>[] results = statisticsHolder.getResultsReported();
    for (Enum<E> result : results) {
      Histogram histogram = statisticsHolder.fetchHistogram(result);
      Histogram copy = histogram.copy();
      histogram.setStartTimeStamp(previousTs);
      histogram.setEndTimeStamp(now);

      Holder previous = this.previous.get(result);
      if (previous == null) {
        try {
          previous = new Holder();
          File hlogFile = new File(this.basedir + File.separatorChar + buildHlogFilename(result.name()));
          hlogFile.getParentFile().mkdirs();
          previous.writer = new HistogramLogWriter(new PrintStream(hlogFile));
          previous.writer.setBaseTime(previousTs);
          previous.writer.outputLogFormatVersion();
          previous.writer.outputBaseTime(previous.writer.getBaseTime());
          previous.writer.outputLegend();
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      } else {
        histogram.subtract(previous.histogram);
      }

      previous.histogram = copy;
      this.previous.put(result, previous);
      previous.writer.outputIntervalHistogram(histogram);
      previousTs = now;
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
        sb.append("reportResponseTime('").append(result.name()).append("', 'Periodic Response Time for ").append(result.name())
            .append("');").append(CRLF);
        sb.append("reportPercentiles('").append(result.name()).append("', 'Response Time percentiles for ").append(result.name())
            .append("');").append(CRLF);
      }
      substituteInFile(reportFile, "//!summary!", sb);
    } catch (Exception e) {
      throw new RuntimeException("Can not report to Html", e);
    }
  }

  /**
   * take a StringBuilder and replace a marker inside a file by the content of that StringBuilder.
   *
   * @param filename path of the file to change
   * @param marker   marker String in file to be replace
   * @param sb       StringBuilder that has the content to put instead of the marker
   * @throws IOException
   */
  private void substituteInFile(final String filename, final String marker, final StringBuilder sb) throws IOException {
    final InputStream in = new FileInputStream(filename);
    Scanner scanner = new Scanner(in);
    StringBuilder fileContents = new StringBuilder();
    try {
      while (scanner.hasNextLine()) {
        fileContents.append(scanner.nextLine()).append(CRLF);
      }
    } finally {
      scanner.close();
    }
    in.close();

    // create template
    byte[] replace = fileContents.toString().replace(marker, sb.toString()).getBytes();

    OutputStream out = new FileOutputStream(new File(filename));
    out.write(replace, 0, replace.length);
    out.close();
  }

  private String buildHlogFilename(String result) {
    return cleanFilename(result) + ".hlog";
  }

  private final static int[] illegalChars = { 34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
      17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47, '@', '.', '\'', '"', '!', '#', '$',
      '%', '^', '&', '*', '(', ')', '\\' };

  private static String cleanFilename(String filename) {
    Arrays.sort(illegalChars);
    StringBuilder cleanName = new StringBuilder();
    for (int i = 0; i < filename.length(); i++) {
      int c = (int)filename.charAt(i);
      if (Arrays.binarySearch(illegalChars, c) < 0) {
        cleanName.append((char)c);
      }
    }
    return cleanName.toString();
  }

  private void deleteDirectory(File path) {
    if (path == null)
      return;
    if (path.exists()) {
      for (File f : path.listFiles()) {
        if (f.isDirectory()) {
          deleteDirectory(f);
          f.delete();
        } else {
          f.delete();
        }
      }
      path.delete();
    }
  }

  private void extractReportFile() throws IOException {
    InputStream in = HtmlReporter.class.getClass().getResourceAsStream("/template/Hdr-template.html");
    OutputStream out = new FileOutputStream(new File(this.reportFile));
    byte[] buffer = new byte[1024];
    int len = in.read(buffer);
    while (len > 0) {
      out.write(buffer, 0, len);
      len = in.read(buffer);
    }
    out.close();
  }

}
