/*
 * Copyright (c) 2014-2018 Aurélien Broszniowski
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

import io.rainfall.utils.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.List;

import static io.rainfall.utils.CompressionUtils.CRLF;

public class HtmlReport {

  private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReport.class);

  private static final CompressionUtils compressionUtils = new CompressionUtils();

  public static void aggregate(Enum[] resultsReported, List<String> srcReportSubdirs, File destReportPath) throws IOException {
    File reportFile = new File(destReportPath, "aggregated-report.html");
    try {
      compressionUtils.extractResources("/report/js", destReportPath.getAbsolutePath() + File.separator + "js");
    } catch (URISyntaxException e) {
      throw new IOException("Error extracting report template", e);
    }
    compressionUtils.extractReportTemplateToFile("/template/Aggregated-template.html", reportFile);

    StringBuilder sb = new StringBuilder();

    sb.append("reportAll([");
    for (String reportSubdir : srcReportSubdirs) {
      if (reportSubdir != null) {
        File reportFolder = new File(reportSubdir);
        sb.append("'").append(reportFolder.getName()).append(File.separator).append("',");
        copyFolder(reportFolder, new File(destReportPath, reportFolder.getName()));
      } else {
        throw new NullPointerException("A Rainfall source report subdir is null");
      }
    }
    sb.setLength(sb.length() - 1);
    sb.append("], [");

    for (Enum result : resultsReported) {
      sb.append("'").append(result.name()).append("',");
    }
    sb.setLength(sb.length() - 1);

    sb.append("]);").append(CRLF);

    compressionUtils.substituteInFile(reportFile.getAbsolutePath(), "//!summary!", sb);
  }

  private static void copyFolder(File src, File dest) throws IOException {
    dest.mkdir();
    File[] files = src.listFiles();
    if (files != null) {
      for (File file : files) {
        if (!file.isDirectory()) {
          copyFile(file, new File(dest, file.getName()));
        } else {
          copyFolder(file, new File(dest, file.getName()));
        }
      }
    }
  }

  private static void copyFile(File source, File dest) throws IOException {
    FileChannel sourceChannel = null;
    FileChannel destChannel = null;
    try {
      sourceChannel = new FileInputStream(source).getChannel();
      destChannel = new FileOutputStream(dest).getChannel();
      destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
    } finally {
      if (sourceChannel != null) sourceChannel.close();
      if (destChannel != null) destChannel.close();
    }
  }

  public static void aggregateInPlace(Enum[] resultsReported, final List<String> srcReportSubdirs, File destReportPath) {
    File reportFile = new File(destReportPath, "aggregated-report.html");
    try {
      compressionUtils.extractResources("/report/js", destReportPath.getAbsolutePath() + File.separator + "js");
      compressionUtils.extractReportTemplateToFile("/template/Aggregated-template.html", reportFile);

      StringBuilder sb = new StringBuilder();

      sb.append("reportAll([");
      for (String reportSubdir : srcReportSubdirs) {
        if (reportSubdir != null) {
          sb.append("'").append(reportSubdir).append(File.separator).append("',");
        } else {
          LOGGER.error("Rainfall client report missing");
        }
      }
      sb.setLength(sb.length() - 1);
      sb.append("], [");

      for (Enum result : resultsReported) {
        sb.append("'").append(result.name()).append("',");
      }
      sb.setLength(sb.length() - 1);

      sb.append("]);")
          .append(CRLF);

      compressionUtils.substituteInFile(reportFile.getAbsolutePath(), "//!summary!", sb);
    } catch (Exception e) {
      throw new RuntimeException("Can not report to Html", e);
    }
  }

}
