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
import org.rainfall.statistics.Statistics;
import org.rainfall.statistics.StatisticsHolder;

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

/**
 * @author Aurelien Broszniowski
 */

public class HtmlReporter<K extends Enum<K>> implements Reporter<K> {

  private String averageLatencyFile = "./target/report/averageLatency.csv";
  private String tpsFile = "./target/report/tps.csv";

  public HtmlReporter(final Class<K> results) {

    File dest = new File("./target/report");
    dest.mkdirs();
    File src;
    try {
      src = new File(HtmlReporter.class.getClass().getResource("/report").toURI());
      copyFolder(src, dest);
      resetFile(averageLatencyFile, results);
      resetFile(tpsFile, results);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not read report template");
    } catch (IOException e) {
      throw new RuntimeException("Can not copy report template");
    }
  }

  private void resetFile(final String filename, Class<K> results) {
    File file = new File(filename);
    file.delete();

    try {
      Writer output =  new BufferedWriter(new FileWriter(filename, false));
      StringBuilder sb = new StringBuilder();
      sb.append("timestamp");
      for (K result : results.getEnumConstants()) {
        sb.append(",").append(result);
      }
      output.append(sb.toString()).append("\n");
      output.close();
    } catch (IOException e) {
      throw new RuntimeException("Can not write report data");
    }
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
  public void report(final StatisticsHolder<K> holder) {

    Writer averageLatencyOutput;
    Writer tpsOutput;
    try {

      averageLatencyOutput = new BufferedWriter(new FileWriter(averageLatencyFile, true));
      tpsOutput = new BufferedWriter(new FileWriter(tpsFile, true));

      Long timestamp = holder.getTimestamp();

      StringBuilder averageLatencySb = new StringBuilder();
      StringBuilder tpsSb = new StringBuilder();
      averageLatencySb.append(timestamp);
      tpsSb.append(timestamp);

      Statistics<K> statistics = holder.getStatistics();
      K[] results = statistics.getKeys();
      for (K result : results) {
        averageLatencySb.append(",").append(String.format("%.2f", statistics.getLatency(result)));
        tpsSb.append(",").append(statistics.getTps(result));
      }
      averageLatencyOutput.append(averageLatencySb.toString()).append("\n");
      tpsOutput.append(tpsSb.toString()).append("\n");

      averageLatencyOutput.close();
      tpsOutput.close();
    } catch (IOException e) {
      throw new RuntimeException("Can not write report data");
    }

  }


}
