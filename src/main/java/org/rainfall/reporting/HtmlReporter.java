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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class HtmlReporter implements Reporter {

  private String averageLatencyFile = "./target/report/averageLatency.csv";
  private String tpsFile = "./target/report/tps.csv";

  public HtmlReporter() {

    File dest = new File("./target/report");
    dest.mkdirs();
    File src;
    try {
      src = new File(HtmlReporter.class.getClass().getResource("/report").toURI());
      copyFolder(src, dest);
      resetFile(averageLatencyFile);
      resetFile(tpsFile);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not read report template");
    } catch (IOException e) {
      throw new RuntimeException("Can not copy report template");
    }
  }

  private void resetFile(final String filename) {
    File file = new File(filename);
    file.delete();
  }

  private void copyFolder(final File src, final File dest) throws IOException {
    if (src.isDirectory()) {
      if (!dest.exists()) {
        dest.mkdir();
      }

      String files[] = src.list();

      for (String file : files) {
        File srcFile = new File(src, file);
        File destFile = new File(dest, file);
        copyFolder(srcFile, destFile);
      }

    } else {
      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dest);

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
    Writer averageLatencyOutput;
    Writer tpsOutput;
 /* TODO  try {
      Statistics statistics = statisticsObserver.getStatistics();

      averageLatencyOutput = new BufferedWriter(new FileWriter(averageLatencyFile, true));
      if (new File(averageLatencyFile).length() == 0)
        addHeader(averageLatencyOutput, statistics.getKeys());
      tpsOutput = new BufferedWriter(new FileWriter(tpsFile, true));
      if (new File(tpsFile).length() == 0)
        addHeader(tpsOutput, statistics.getKeys());

      Long timestamp = statisticsObserver.getTimestamp();

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
    } catch (IOException e) {
      throw new RuntimeException("Can not write report data");
    }*/
  }

  private void addHeader(Writer output, Result[] keys) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("timestamp");
    for (Result key : keys) {
      sb.append(",").append(key);
    }
    output.append(sb.toString()).append("\n");
  }
}
