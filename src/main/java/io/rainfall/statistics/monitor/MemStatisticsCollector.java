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
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by KECL on 8/18/2016.
 */
public class MemStatisticsCollector  implements StatisticsCollector {

  private static final MemoryMXBean MEM_BEAN = ManagementFactory.getMemoryMXBean();
  private static final String MEM_STATS = "Memory Utilization";

  private Writer memOutput;

  public static class MemStats {
    public enum Header {
      MEMORY_UTILIZATION
    }

    private final long startTimestamp;
    private final long memUtilization;

    public MemStats(long startTimestamp, long memUtilization) {
      this.startTimestamp = startTimestamp;
      this.memUtilization = memUtilization;
    }
  }

  private final Queue<MemStatisticsCollector.MemStats> memStatsQueue = new ConcurrentLinkedQueue<MemStatisticsCollector.MemStats>();

  @Override
  public void initialize() {}

  @Override
  public void terminate() {}

  @Override
  public Exporter peek() {
    memStatsQueue.add(new MemStatisticsCollector.MemStats(System.currentTimeMillis(), MEM_BEAN.getHeapMemoryUsage().getUsed()));
    return new MemStatisticsCollector.MemStatisticsExporter();
  }

  @Override
  public String getName() {
    return MEM_STATS;
  }

  public class MemStatisticsExporter implements HtmlExporter, TextExporter {

    private HtmlReporter reporterUtils = new HtmlReporter();
    private String memFile = "memory.csv";
    List<MemStatisticsCollector.MemStats> memStatsList;
    long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();

    public MemStatisticsExporter() {
      memStatsList = new ArrayList<MemStatisticsCollector.MemStats>();
      while (true) {
        MemStatisticsCollector.MemStats memStats = memStatsQueue.poll();
        if (memStats == null) break;
        memStatsList.add(memStats);
      }
    }

    @Override
    public void ouputText() {
      //TODO
    }

    @Override
    public void ouputCsv(final String basedir) throws Exception {
      String memFilename = basedir + File.separatorChar + this.memFile;

      memOutput = new BufferedWriter(new FileWriter(memFilename, true));
      if (new File(memFilename).length() == 0)
        reporterUtils.addHeader(memOutput, MemStatisticsCollector.MemStats.Header.values());

      for (MemStatisticsCollector.MemStats memStats : this.memStatsList) {
        memOutput.append(toCsv(memStats)).append("\n");
      }

      memOutput.close();
    }

    @Override
    public String outputHtml() {
      return "    function reportMemory(filename, title) {\n" +
             "        $(\"#memory-box\").append(\"<div id='\" + filename + 'memory' + \"' style='height: 550px;width: 1200px;'><div class='title'/><div class='graph'/></div>\");\n" +
             "        d3.csv(filename + \".csv\", function (data) { processData(data, filename + 'memory', title, 'Memory Utilization') });\n" +
             "    }\n" +
             "\n" +
             "$('body').append('<div class=\"border\"><h1><a name=\"memory\">Memory Utilization</a></h1><div id=\"memory-box\"></div></div><br/>');\n" +
             "reportMemory('memory', 'Memory Utilization');\n";
    }

    private String toCsv(MemStatisticsCollector.MemStats memStats) {
      return String.valueOf(reporterUtils.formatTimestampInNano(jvmStartTime + memStats.startTimestamp)) + "," +
             memStats.memUtilization;
    }
  }
}