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
package io.rainfall.statistics.monitor;


import io.rainfall.reporting.HtmlReporter;
import io.rainfall.statistics.collector.StatisticsCollector;
import io.rainfall.statistics.exporter.Exporter;
import io.rainfall.statistics.exporter.HtmlExporter;
import io.rainfall.statistics.exporter.TextExporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

/**
 * @author Ludovic Orban
 */
public class GcStatisticsCollector implements StatisticsCollector {

  private static final String GARBAGE_COLLECTION_NOTIFICATION = "com.sun.management.gc.notification";

  private Writer output;
  public final static String GC_STATS = "GC_STATS";

  public HtmlReporter reporterUtils = new HtmlReporter();

  public static class GcStats {
    public enum Header {
      DURATION, ACTION, CAUSE, NAME
    }

    private final long startTimestamp;
    private final long duration;
    private final String action;
    private final String cause;
    private final String name;

    public GcStats(long duration, String action, long startTimestamp, String cause, String name) {
      this.duration = duration;
      this.action = action;
      this.startTimestamp = startTimestamp;
      this.cause = cause;
      this.name = name;
    }

    public long getStartTimestamp() {
      return startTimestamp;
    }

    public long getDuration() {
      return duration;
    }

    public String getAction() {
      return action;
    }

    public String getCause() {
      return cause;
    }

    public String getName() {
      return name;
    }
  }

  private final Queue<GcStats> gcStatsQueue = new ConcurrentLinkedQueue<GcStats>();

  private final NotificationListener listener = new NotificationListener() {
    @Override
    public void handleNotification(Notification notification, Object handback) {
      if (notification.getType().equals(GARBAGE_COLLECTION_NOTIFICATION)) {
        CompositeData userData = (CompositeData)notification.getUserData();
        CompositeData gcInfo = (CompositeData)userData.get("gcInfo");

        GcStats gcStats = new GcStats(
            (Long)gcInfo.get("duration"),
            (String)userData.get("gcAction"),
            (Long)gcInfo.get("startTime"),
            (String)userData.get("gcCause"),
            (String)userData.get("gcName")
        );
        gcStatsQueue.add(gcStats);
      }
    }
  };

  @Override
  public void initialize() {
    List<GarbageCollectorMXBean> gcMxBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
      NotificationEmitter emitter = (NotificationEmitter)gcMxBean;
      emitter.addNotificationListener(listener, null, null);
    }
  }

  @Override
  public void terminate() {
    List<GarbageCollectorMXBean> gcMxBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
      NotificationEmitter emitter = (NotificationEmitter)gcMxBean;

      try {
        emitter.removeNotificationListener(listener);
      } catch (ListenerNotFoundException e) {
        //
      }
    }
  }

  @Override
  public Exporter peek() {
    return new GcStatisticsExporter();
  }

  @Override
  public String getName() {
    return GC_STATS;
  }

  public class GcStatisticsExporter implements HtmlExporter, TextExporter {

    private HtmlReporter reporterUtils = new HtmlReporter();
    private String gcFile = "gc.csv";
    List<GcStats> gcStatsList;
    long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();

    public GcStatisticsExporter() {
      gcStatsList = new ArrayList<GcStats>();
      while (true) {
        GcStats gcStats = gcStatsQueue.poll();
        if (gcStats == null) break;
        gcStatsList.add(gcStats);
      }
    }

    @Override
    public void ouputText() {
      //TODO
    }

    @Override
    public void ouputCsv(final String basedir) throws Exception {
      String gcFilename = basedir + File.separatorChar + this.gcFile;

      output = new BufferedWriter(new FileWriter(gcFilename, true));
      if (new File(gcFilename).length() == 0)
        reporterUtils.addHeader(output, GcStatisticsCollector.GcStats.Header.values());

      for (GcStatisticsCollector.GcStats gcStats : this.gcStatsList) {
        output.append(toCsv(gcStats)).append("\n");
      }

      output.close();
    }

    @Override
    public String outputHtml() {
      return "    function reportGc(filename, title) {\n" +
             "        $(\"#gc-box\").append(\"<div id='\" + filename + 'gc' + \"' style='height: 550px;width: 1200px;'><div class='title'/><div class='graph'/></div>\");\n" +
             "        d3.csv(filename + \".csv\", function (data) { processData(data, filename + 'gc', title, 'Pause Time (ms)') });\n" +
             "    }\n" +
             "\n" +
             "$('body').append('<div class=\"border\"><h1><a name=\"gc\">Garbage Collection</a></h1><div id=\"gc-box\"></div></div><br/>');\n" +
             "reportGc('gc', 'GC Time');\n";
    }

    private String toCsv(GcStats gcStats) {
      return String.valueOf(reporterUtils.formatTimestampInNano(jvmStartTime + gcStats.getStartTimestamp())) + "," +
             gcStats.getDuration() + "," +
             gcStats.getAction() + "," +
             gcStats.getCause() + "," +
             gcStats.getName();
    }

  }


}
