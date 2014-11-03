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
import org.rainfall.statistics.StatisticsObserver;
import org.rainfall.statistics.StatisticsObserversFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author Aurelien Broszniowski
 */

public class HtmlReporter implements Reporter {

  private String basedir = "./target/rainfall-report";
  private String averageLatencyFile = "averageLatency.csv";
  private String tpsFile = "tps.csv";
  private String reportFile = this.basedir + File.pathSeparatorChar + "report.html";

  public HtmlReporter() {
    try {
      File src = new File(HtmlReporter.class.getClass().getResource("/report").toURI());
      File dest = new File(this.basedir);
      copyFolder(src, dest);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not read report template");
    } catch (IOException e) {
      throw new RuntimeException("Can not copy report template");
    }
  }

  private void copyFolder(final File src, final File dst) throws IOException {
    if (src.isDirectory()) {
      deleteDirectory(dst);
      dst.mkdirs();

      String files[] = src.list();

      for (String file : files) {
        File srcFile = new File(src, file);
        File destFile = new File(dst, file);
        copyFolder(srcFile, destFile);
      }

    } else {
      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dst);

      byte[] buffer = new byte[1024];

      int length;
      while ((length = in.read(buffer)) > 0) {
        out.write(buffer, 0, length);
      }

      in.close();
      out.close();
    }
  }

  @Override
  public void report(final StatisticsObserversFactory observersFactory) {
    try {
      if (!new File(reportFile).exists()) {
        copyReportTemplate(observersFactory.getStatisticObserverKeys());
      }

      Set<String> keys = observersFactory.getStatisticObserverKeys();
      for (String key : keys) {
        reportToFile(key, observersFactory.getStatisticObserver(key));
      }
      reportToFile("total", observersFactory.getTotalStatisticObserver());

    } catch (IOException e) {
      throw new RuntimeException("Can not write report data");
    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not write report data");
    }
  }

  private void reportToFile(final String key, final StatisticsObserver statisticsObserver) throws IOException {
    if (statisticsObserver != null) {
      Statistics statistics = statisticsObserver.getStatistics();

      String avgFilename = this.basedir + File.separatorChar + getAverageLatencyFilename(key);
      String tpsFilename = this.basedir + File.separatorChar + getTpsFilename(key);

      Writer averageLatencyOutput;
      Writer tpsOutput;

      averageLatencyOutput = new BufferedWriter(new FileWriter(avgFilename, true));
      if (new File(avgFilename).length() == 0)
        addHeader(averageLatencyOutput, statistics.getKeys());
      tpsOutput = new BufferedWriter(new FileWriter(tpsFilename, true));
      if (new File(tpsFilename).length() == 0)
        addHeader(tpsOutput, statistics.getKeys());

      String timestamp = formatTimestampInNano(statisticsObserver.getTimestamp());

      StringBuilder averageLatencySb = new StringBuilder();
      StringBuilder tpsSb = new StringBuilder();
      averageLatencySb.append(timestamp);
      tpsSb.append(timestamp);

      Result[] results = statistics.getKeys();
      for (Result result : results) {
        averageLatencySb.append(",").append(String.format("%.2f", statistics.getLatency(result)));
        tpsSb.append(",").append(statistics.getTps(result));
      }
      averageLatencyOutput.append(averageLatencySb.toString()).append("\n");
      tpsOutput.append(tpsSb.toString()).append("\n");

      averageLatencyOutput.close();
      tpsOutput.close();
    }
  }

  private String getTpsFilename(final String key) {
    return key + "-" + this.tpsFile;
  }

  private String getAverageLatencyFilename(final String key) {
    return key + "-" + this.averageLatencyFile;
  }

  private void copyReportTemplate(final Set<String> keys) throws IOException, URISyntaxException {
    File src = new File(HtmlReporter.class.getClass().getResource("/template/Tps-template.html").toURI());
    StringBuilder sb = new StringBuilder();
    for (String key : keys) {
      sb.append("reportTps('").append(getTpsFilename(key)).append("');").append(System.getProperty("line.separator"));
    }
    for (String key : keys) {
      sb.append("reportTps('")
          .append(getAverageLatencyFilename(key))
          .append("');")
          .append(System.getProperty("line.separator"));
    }

    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(reportFile);

    byte[] buffer = new byte[(int)src.length()];

    int length;
    while ((length = in.read(buffer)) > 0) {
      String bufferString = new String (buffer);
      String replaced = bufferString.replace("!report!", sb.toString());
      out.write(replaced.getBytes(), 0, length);
    }

    in.close();
    out.close();
  }

  private String formatTimestampInNano(final long timestamp) {
    Calendar calendar = GregorianCalendar.getInstance(TimeZone.getDefault());
    calendar.setTime(new Date(timestamp / 1000000));
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    return sdf.format(calendar.getTime());
  }

  private void addHeader(Writer output, Result[] keys) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("timestamp");
    for (Result key : keys) {
      sb.append(",").append(key.value());
    }
    output.append(sb.toString()).append("\n");
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
}
