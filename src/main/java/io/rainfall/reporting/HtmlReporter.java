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
import io.rainfall.configuration.ReportType;
import io.rainfall.statistics.StatisticsPeek;
import io.rainfall.statistics.StatisticsPeekHolder;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static io.rainfall.configuration.ReportType.CUMULATIVE;
import static io.rainfall.configuration.ReportType.CUMULATIVE_AND_PERIODIC;
import static io.rainfall.configuration.ReportType.PERIODIC;

/**
 * @author Aurelien Broszniowski
 */

public class HtmlReporter<E extends Enum<E>> extends Reporter<E> {

  private String basedir;
  private String averageLatencyFile = "averageLatency.csv";
  private String tpsFile = "tps.csv";
  private String reportFile;
  private final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
  private final static String CRLF = System.getProperty("line.separator");
  private Calendar calendar = GregorianCalendar.getInstance(TimeZone.getDefault());
  private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

  public HtmlReporter() {
    try {
      this.basedir = new File("target/rainfall-report").getAbsoluteFile().getAbsolutePath();
      this.reportFile = this.basedir + File.separatorChar + "report.html";

      deleteDirectory(new File(this.basedir));

      if (jarFile.isFile()) {  // Run with JAR file
        extractFromJar("/report", this.basedir);
      } else {
        extractFromPath(new File(HtmlReporter.class.getClass().getResource("/report").toURI()), new File(this.basedir));
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not read report template");
    } catch (IOException e) {
      throw new RuntimeException("Can not copy report template");
    }
  }

  private void extractFromPath(final File src, final File dst) throws IOException {
    if (src.isDirectory()) {
      dst.mkdirs();

      String files[] = src.list();

      for (String file : files) {
        File srcFile = new File(src, file);
        File destFile = new File(dst, file);
        extractFromPath(srcFile, destFile);
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
  public void report(final StatisticsPeekHolder<E> statisticsHolder) {
    try {
      if (!new File(reportFile).exists()) {
        copyReportTemplate(statisticsHolder.getStatisticsPeeksNames());
      }

      StatisticsPeek<E> totalStatisticsPeeks = statisticsHolder.getTotalStatisticsPeeks();
      Set<String> keys = statisticsHolder.getStatisticsPeeksNames();

      if (getReportType() == CUMULATIVE_AND_PERIODIC || getReportType() == CUMULATIVE) {
        for (String key : keys) {
          StatisticsPeek<E> statisticsPeeks = statisticsHolder.getStatisticsPeeks(key);
          logCumulativeStats(key, statisticsPeeks);
        }

        if (totalStatisticsPeeks != null)
          logCumulativeStats("total", totalStatisticsPeeks);
      }

      if (getReportType() == CUMULATIVE_AND_PERIODIC || getReportType() == PERIODIC) {
        for (String key : keys) {
          StatisticsPeek<E> statisticsPeeks = statisticsHolder.getStatisticsPeeks(key);
          logPeriodicStats(key, statisticsPeeks);
        }

        if (totalStatisticsPeeks != null)
          logPeriodicStats("total", totalStatisticsPeeks);
      }

    } catch (IOException e) {
      throw new RuntimeException("Can not write report data");
    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not write report data");
    }
  }

  @Override
  public void summarize(final StatisticsPeekHolder<E> statisticsHolder) {
    //TODO : add summary at the end of the html ?
  }

  private void logPeriodicStats(String name, StatisticsPeek<E> statisticsPeek) throws IOException {
    String avgFilename = this.basedir + File.separatorChar + getAverageLatencyFilename(name, PERIODIC);
    String tpsFilename = this.basedir + File.separatorChar + getTpsFilename(name, PERIODIC);

    Writer averageLatencyOutput;
    Writer tpsOutput;

    averageLatencyOutput = new BufferedWriter(new FileWriter(avgFilename, true));
    if (new File(avgFilename).length() == 0)
      addHeader(averageLatencyOutput, statisticsPeek.getKeys());
    tpsOutput = new BufferedWriter(new FileWriter(tpsFilename, true));
    if (new File(tpsFilename).length() == 0)
      addHeader(tpsOutput, statisticsPeek.getKeys());

    String timestamp = formatTimestampInNano(statisticsPeek.getTimestamp());

    StringBuilder averageLatencySb = new StringBuilder(timestamp);
    StringBuilder tpsSb = new StringBuilder(timestamp);

    Enum<E>[] keys = statisticsPeek.getKeys();
    for (Enum<E> key : keys) {
      averageLatencySb.append(",").append(String.format("%.2f", (statisticsPeek.getPeriodicAverageLatencyInMs(key))));
      tpsSb.append(",").append(statisticsPeek.getPeriodicTps(key));
    }
    averageLatencyOutput.append(averageLatencySb.toString()).append("\n");
    tpsOutput.append(tpsSb.toString()).append("\n");

    averageLatencyOutput.close();
    tpsOutput.close();
  }

  private void logCumulativeStats(String name, StatisticsPeek<E> statisticsPeek) throws IOException {
    String avgFilename = this.basedir + File.separatorChar + getAverageLatencyFilename(name, CUMULATIVE);
    String tpsFilename = this.basedir + File.separatorChar + getTpsFilename(name, CUMULATIVE);

    Writer averageLatencyOutput;
    Writer tpsOutput;

    averageLatencyOutput = new BufferedWriter(new FileWriter(avgFilename, true));
    if (new File(avgFilename).length() == 0)
      addHeader(averageLatencyOutput, statisticsPeek.getKeys());
    tpsOutput = new BufferedWriter(new FileWriter(tpsFilename, true));
    if (new File(tpsFilename).length() == 0)
      addHeader(tpsOutput, statisticsPeek.getKeys());

    String timestamp = formatTimestampInNano(statisticsPeek.getTimestamp());

    StringBuilder averageLatencySb = new StringBuilder(timestamp);
    StringBuilder tpsSb = new StringBuilder(timestamp);

    Enum<E>[] keys = statisticsPeek.getKeys();
    for (Enum<E> key : keys) {
      averageLatencySb.append(",").append(String.format("%.2f", (statisticsPeek.getCumulativeAverageLatencyInMs(key))));
      tpsSb.append(",").append(statisticsPeek.getCumulativeTps(key));
    }
    averageLatencyOutput.append(averageLatencySb.toString()).append("\n");
    tpsOutput.append(tpsSb.toString()).append("\n");

    averageLatencyOutput.close();
    tpsOutput.close();
  }

  /**
   * extract the subdirectory from a jar on the classpath to {@code writeDirectory}
   *
   * @param sourceDirectory directory (in a jar on the classpath) to extract
   * @param writeDirectory  the location to extract to
   * @throws IOException if an IO exception occurs
   */
  public void extractFromJar(String sourceDirectory, String writeDirectory) throws IOException {
    final URL dirURL = getClass().getResource(sourceDirectory);
    final String path = sourceDirectory.substring(1);

    if ((dirURL != null) && dirURL.getProtocol().equals("jar")) {
      final JarURLConnection jarConnection = (JarURLConnection)dirURL.openConnection();
      System.out.println("jarConnection is " + jarConnection);

      final ZipFile jar = jarConnection.getJarFile();

      final Enumeration<? extends ZipEntry> entries = jar.entries(); // gives ALL entries in jar

      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final String name = entry.getName();
        // System.out.println( name );
        if (!name.startsWith(path)) {
          // entry in wrong subdir -- don't copy
          continue;
        }
        final String entryTail = name.substring(path.length());

        final File f = new File(writeDirectory + File.separator + entryTail);
        if (entry.isDirectory()) {
          // if its a directory, create it
          final boolean bMade = f.mkdir();
          System.out.println((bMade ? "  creating " : "  unable to create ") + name);
        } else {
          System.out.println("  writing  " + name);
          final InputStream is = jar.getInputStream(entry);
          final OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
          final byte buffer[] = new byte[4096];
          int readCount;
          // write contents of 'is' to 'os'
          while ((readCount = is.read(buffer)) > 0) {
            os.write(buffer, 0, readCount);
          }
          os.close();
          is.close();
        }
      }

    } else if (dirURL == null) {
      throw new IllegalStateException("can't find " + sourceDirectory + " on the classpath");
    } else {
      // not a "jar" protocol URL
      throw new IllegalStateException("don't know how to handle extracting from " + dirURL);
    }
  }

  private String getTpsFilename(String key, ReportType reportType) {
    return cleanFilename(key) + "-" + reportType.name() + "-" + this.tpsFile;
  }

  private String getAverageLatencyFilename(String key, ReportType reportType) {
    return cleanFilename(key) + "-" + reportType.name() + "-" + this.averageLatencyFile;
  }

  private final static int[] illegalChars = { 34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
      17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47, '@', '.', '\'', '"', '!', '#', '$',
      '%', '^', '&', '*', '(', ')', '\\' };

  public String cleanFilename(String filename) {
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

  private void copyReportTemplate(final Set<String> names) throws IOException, URISyntaxException {
    StringBuilder sb = new StringBuilder();
    // Cumulative
    if (getReportType() == CUMULATIVE_AND_PERIODIC || getReportType() == CUMULATIVE) {
      for (String name : names) {
        String tpsFilename = getTpsFilename(name, CUMULATIVE);
        sb.append("report('").append(tpsFilename.substring(0, tpsFilename.length() - 4))
            .append("', 'Cumulative TPS - ").append(name)
            .append("');").append(CRLF);
      }
      sb.append("report('total-cumulative-tps', 'Cumulative Total TPS');").append(CRLF);
      for (String key : names) {
        String averageLatencyFilename = getAverageLatencyFilename(key, CUMULATIVE);
        sb.append("report('")
            .append(averageLatencyFilename.substring(0, averageLatencyFilename.length() - 4))
            .append("', 'Cumulative Average latency - ").append(key)
            .append("');").append(CRLF);
      }
      sb.append("report('total-cumulative-averageLatency', 'Cumulative Average Latency of all');").append(CRLF);
    }

    // Periodic
    if (getReportType() == CUMULATIVE_AND_PERIODIC || getReportType() == PERIODIC) {
      for (String name : names) {
        String tpsFilename = getTpsFilename(name, PERIODIC);
        sb.append("report('").append(tpsFilename.substring(0, tpsFilename.length() - 4))
            .append("', 'Periodic TPS - ").append(name)
            .append("');").append(CRLF);
      }
      sb.append("report('total-periodic-tps', 'Periodic Total TPS');").append(CRLF);
      for (String key : names) {
        String averageLatencyFilename = getAverageLatencyFilename(key, PERIODIC);
        sb.append("report('")
            .append(averageLatencyFilename.substring(0, averageLatencyFilename.length() - 4))
            .append("', 'Periodic Average latency - ").append(key)
            .append("');").append(CRLF);
      }
      sb.append("report('total-periodic-averageLatency', 'Periodic Average Latency of all');").append(CRLF);
    }

    InputStream in = HtmlReporter.class.getClass().getResourceAsStream("/template/Tps-template.html");
    Scanner scanner = new Scanner(in);
    StringBuilder fileContents = new StringBuilder();
    try {
      while (scanner.hasNextLine()) {
        fileContents.append(scanner.nextLine() + CRLF);
      }
    } finally {
      scanner.close();
    }
    in.close();

    // create template
    byte[] replace = fileContents.toString().replace("!report!", sb.toString()).getBytes();
    OutputStream out = new FileOutputStream(reportFile);
    out.write(replace, 0, replace.length);
    out.close();
  }

  private String formatTimestampInNano(final long timestamp) {
    calendar.setTime(new Date(timestamp));
    return sdf.format(calendar.getTime());
  }

  private void addHeader(Writer output, Enum[] keys) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("timestamp");
    for (Enum key : keys) {
      sb.append(",").append(key.name());
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
