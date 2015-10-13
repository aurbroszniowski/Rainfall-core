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
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * @author Aurelien Broszniowski
 */

public class HtmlReporter<E extends Enum<E>> extends Reporter<E> {

  private String basedir;
  private String averageLatencyFile = "averageLatency.csv";
  private String tpsFile = "tps.csv";
  private String percentilesFile = "total-percentiles.csv";
  private String reportFile;
  private final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
  private final static String CRLF = System.getProperty("line.separator");
  private Calendar calendar = GregorianCalendar.getInstance(TimeZone.getDefault());
  private SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

  public HtmlReporter() {
    this("target/rainfall-report");
  }

  public HtmlReporter(String outputPath) {
    try {
      this.basedir = new File(outputPath).getAbsoluteFile().getAbsolutePath();
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
  public void report(final StatisticsPeekHolder<E> statisticsPeekHolder) {
    try {
      if (!new File(reportFile).exists()) {
        copyReportTemplate(statisticsPeekHolder);
      }

      StatisticsPeek<E> totalStatisticsPeeks = statisticsPeekHolder.getTotalStatisticsPeeks();

      Set<String> keys = statisticsPeekHolder.getStatisticsPeeksNames();

      for (String key : keys) {
        StatisticsPeek<E> statisticsPeeks = statisticsPeekHolder.getStatisticsPeeks(key);
        logPeriodicStats(key, statisticsPeeks, statisticsPeekHolder.getResultsReported());
      }

      if (totalStatisticsPeeks != null)
        logPeriodicStats("total", totalStatisticsPeeks, statisticsPeekHolder.getResultsReported());

    } catch (IOException e) {
      throw new RuntimeException("Can not write report data");
    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not write report data");
    }
  }

  @Override
  public void summarize(final StatisticsHolder<E> statisticsHolder) {
    StringBuilder sb = new StringBuilder();
    Enum<E>[] results = statisticsHolder.getResultsReported();
    try {
      for (Enum<E> result : results) {
        Histogram histogram = statisticsHolder.getHistogram(result);
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
            .append("', 'Reponse Time percentiles for ").append(result.name())
            .append("', '" + mean + "', '" + maxValue)
            .append("');").append(CRLF);
      }
      substituteInFile(new FileInputStream(new File(reportFile)), reportFile, "//!summary!", sb);


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
          // if its a directory, create it -- REVIEW @yzhang and also any intermediate dirs
          final boolean bMade = f.mkdirs();
          System.out.println((bMade ? "  creating " : "  unable to create ") + f.getCanonicalPath());
        } else {
          System.out.println("  writing  " + f.getCanonicalPath());
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

  private String getTpsFilename(String key) {
    return cleanFilename(key) + "-" + this.tpsFile;
  }

  private String getAverageLatencyFilename(String key) {
    return cleanFilename(key) + "-" + this.averageLatencyFile;
  }

  private String getPercentilesFilename(String result) {
    return cleanFilename(result) + "-" + this.percentilesFile;
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

  private void copyReportTemplate(final StatisticsPeekHolder<E> peek) throws IOException, URISyntaxException {
    StringBuilder sb = new StringBuilder();

    Set<String> names = peek.getStatisticsPeeksNames();
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
          .append("', 'Periodic Reponse Time - ").append(key)
          .append("');").append(CRLF);
    }
    sb.append("reportResponseTime('total-averageLatency', 'Periodic Average Response Time of all entities');")
        .append(CRLF);

    InputStream in = HtmlReporter.class.getClass().getResourceAsStream("/template/Tps-template.html");
    substituteInFile(in, reportFile, "//!report!", sb);
  }

  /**
   * take a StringBuilder and replace a marker inside a file by the content of that StringBuilder.
   *
   * @param in         InputStream of the source file
   * @param outputFile the destination file
   * @param marker     marker String in file to be replace
   * @param sb         StringBuilder that has the content to put instead of the marker
   * @throws IOException
   */
  private void substituteInFile(final InputStream in, final String outputFile, final String marker, final StringBuilder sb) throws IOException {
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
    OutputStream out = new FileOutputStream(outputFile);
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
