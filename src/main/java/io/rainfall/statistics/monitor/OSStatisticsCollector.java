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
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by KECL on 8/18/2016.
 */
public class OSStatisticsCollector implements StatisticsCollector {

  private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getOperatingSystemMXBean();
  private static final String OS_STATS = "Processor Utilization";
  private static final int AVAILABLE_PROCESSORS = OS_BEAN.getAvailableProcessors();

  private Writer osOutput;

  public static class OSStats {
    public enum Header {
      PROCESSOR_UTILIZATION
    }

    private final long startTimestamp;
    private final double procUtilization;

    public OSStats(long startTimestamp, double procUtilization) {
      this.startTimestamp = startTimestamp;
      this.procUtilization = procUtilization;
    }
  }

  private final Queue<OSStatisticsCollector.OSStats> osStatsQueue = new ConcurrentLinkedQueue<OSStatisticsCollector.OSStats>();

  @Override
  public void initialize() {}

  @Override
  public void terminate() {}

  @Override
  public Exporter peek() {
    osStatsQueue.add(new OSStats(System.currentTimeMillis(), OS_BEAN.getSystemLoadAverage() / AVAILABLE_PROCESSORS * 100.0));
    return new OSStatisticsCollector.OSStatisticsExporter();
  }

  @Override
  public String getName() {
    return OS_STATS;
  }

  public class OSStatisticsExporter implements HtmlExporter, TextExporter {

    private HtmlReporter reporterUtils = new HtmlReporter();
    private String osFile = "os.csv";
    List<OSStatisticsCollector.OSStats> osStatsList;
    long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();

    public OSStatisticsExporter() {
      osStatsList = new ArrayList<OSStatisticsCollector.OSStats>();
      while (true) {
        OSStatisticsCollector.OSStats osStats = osStatsQueue.poll();
        if (osStats == null) break;
        osStatsList.add(osStats);
      }
    }

    @Override
    public void ouputText() {
      //TODO
    }

    @Override
    public void ouputCsv(final String basedir) throws Exception {
      String osFilename = basedir + File.separatorChar + this.osFile;

      osOutput = new BufferedWriter(new FileWriter(osFilename, true));
      if (new File(osFilename).length() == 0)
        reporterUtils.addHeader(osOutput, OSStatisticsCollector.OSStats.Header.values());

      for (OSStatisticsCollector.OSStats osStats : this.osStatsList) {
        osOutput.append(toCsv(osStats)).append("\n");
      }

      osOutput.close();
    }

    @Override
    public String outputHtml() {
      return "    function reportOS(filename, title) {\n" +
             "        $(\"#os-box\").append(\"<div id='\" + filename + 'os' + \"' style='height: 550px;width: 1200px;'><div class='title'/><div class='graph'/></div>\");\n" +
             "        d3.csv(filename + \".csv\", function (data) { processData(data, filename + 'os', title, 'Processor Utilization') });\n" +
             "    }\n" +
             "\n" +
             "$('body').append('<div class=\"border\"><h1><a name=\"os\">Processor Utilization</a></h1><div id=\"os-box\"></div></div><br/>');\n" +
             "reportOS('os', 'Processor Utilization');\n";
    }

    private String toCsv(OSStatisticsCollector.OSStats osStats) {
      return String.valueOf(reporterUtils.formatTimestampInNano(jvmStartTime + osStats.startTimestamp)) + "," +
             osStats.procUtilization;
    }
  }
}
