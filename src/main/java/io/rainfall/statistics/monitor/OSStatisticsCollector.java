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

/**
 * Created by KECL on 8/18/2016.
 */
public class OSStatisticsCollector implements StatisticsCollector {

  private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getOperatingSystemMXBean();
  private static final String OS_STATS = "Processor Utilization";
  private static final int AVAILABLE_PROCESSORS = OS_BEAN.getAvailableProcessors();

  private Writer osOutput;

  public enum Header {
    PROCESSOR_UTILIZATION
  }

  @Override
  public void initialize() {}

  @Override
  public void terminate() {}

  @Override
  public Exporter peek() {
    return new OSStatisticsCollector.OSStatisticsExporter(System.currentTimeMillis(), OS_BEAN.getSystemLoadAverage() / AVAILABLE_PROCESSORS * 100.0);
  }

  @Override
  public String getName() {
    return OS_STATS;
  }

  public class OSStatisticsExporter implements HtmlExporter, TextExporter {

    private HtmlReporter reporterUtils = new HtmlReporter();
    private String osFile = "os.csv";
    private long timestamp;
    private double processorUsage;

    public OSStatisticsExporter(long timestamp, double processorUsage) {
      this.timestamp = timestamp;
      this.processorUsage = processorUsage;
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
        reporterUtils.addHeader(osOutput, OSStatisticsCollector.Header.values());

      osOutput.append(reporterUtils.formatTimestampInNano(timestamp) + "," + processorUsage);
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
  }
}
