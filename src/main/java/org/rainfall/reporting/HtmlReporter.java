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
import org.rainfall.statistics.RuntimeStatisticsObserversHolder;
import org.rainfall.statistics.Statistics;
import org.rainfall.statistics.StatisticsObserver;

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

/**
 * @author Aurelien Broszniowski
 */

public class HtmlReporter implements Reporter {

  private String basedir = "./target/rainfall-report";
  private String averageLatencyFile = "averageLatency.csv";
  private String tpsFile = "tps.csv";
  private String reportFile = this.basedir + File.pathSeparatorChar + "report.html";
  private final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
  private final static String CRLF = System.getProperty("line.separator");

  public HtmlReporter() {
    try {
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
  public void report(final RuntimeStatisticsObserversHolder observersFactory) {
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
        averageLatencySb.append(",").append(String.format("%.2f", statistics.getAverageLatency(result)));
        tpsSb.append(",").append(statistics.getTps(result));
      }
      averageLatencyOutput.append(averageLatencySb.toString()).append("\n");
      tpsOutput.append(tpsSb.toString()).append("\n");

      averageLatencyOutput.close();
      tpsOutput.close();
    }
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

  private String getTpsFilename(final String key) {
    return cleanFilename(key + "-" + this.tpsFile);
  }

  private String getAverageLatencyFilename(final String key) {
    return cleanFilename(key + "-" + this.averageLatencyFile);
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

  private void copyReportTemplate(final Set<String> keys) throws IOException, URISyntaxException {
    StringBuilder sb = new StringBuilder();
    for (String key : keys) {
      sb.append("reportTps('").append(getTpsFilename(key)).append("');").append(CRLF);
    }
    for (String key : keys) {
      sb.append("reportTps('")
          .append(getAverageLatencyFilename(key))
          .append("');")
          .append(CRLF);
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

    byte[] replace = fileContents.toString().replace("!report!", sb.toString()).getBytes();
    OutputStream out = new FileOutputStream(reportFile);
    out.write(replace, 0, replace.length);
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
