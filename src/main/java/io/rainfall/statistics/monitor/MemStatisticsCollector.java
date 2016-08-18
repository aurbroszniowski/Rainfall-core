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

/**
 * Created by KECL on 8/18/2016.
 */
public class MemStatisticsCollector  implements StatisticsCollector {

  private static final MemoryMXBean MEM_BEAN = ManagementFactory.getMemoryMXBean();
  private static final String MEM_STATS = "Memory Utilization";

  private Writer memOutput;

  public enum Header {
    MEMORY_UTILIZATION
  }

  @Override
  public void initialize() {}

  @Override
  public void terminate() {}

  @Override
  public Exporter peek() {
    return new MemStatisticsCollector.MemStatisticsExporter(System.currentTimeMillis(), MEM_BEAN.getHeapMemoryUsage().getUsed());
  }

  @Override
  public String getName() {
    return MEM_STATS;
  }

  public class MemStatisticsExporter implements HtmlExporter, TextExporter {

    private HtmlReporter reporterUtils = new HtmlReporter();
    private String memFile = "memory.csv";
    long timestamp;
    long memoryUsage;

    public MemStatisticsExporter(long timestamp, long memoryUsage) {
      this.timestamp = timestamp;
      this.memoryUsage = memoryUsage;
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
        reporterUtils.addHeader(memOutput, MemStatisticsCollector.Header.values());

      memOutput.append(reporterUtils.formatTimestampInNano(timestamp) + "," + memoryUsage);
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
  }
}