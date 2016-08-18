package io.rainfall.statistics.monitor;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;

/**
 * @author Aurelien Broszniowski
 */
public class PerformanceMonitor {
  private int availableProcessors = getOperatingSystemMXBean().getAvailableProcessors();
  private long lastSystemTime = 0;
  private long lastProcessCpuTime = 0;

  public synchronized double getCpuUsage() {
    if (lastSystemTime == 0) {
      baselineCounters();
      return 0;
    }

    long systemTime = System.nanoTime();
    long processCpuTime = 0;

    if (getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
      processCpuTime = ((OperatingSystemMXBean)getOperatingSystemMXBean()).getProcessCpuTime();
    }

    double cpuUsage = (double)(processCpuTime - lastProcessCpuTime) / (systemTime - lastSystemTime);

    lastSystemTime = systemTime;
    lastProcessCpuTime = processCpuTime;

    return cpuUsage / availableProcessors;
  }

  private void baselineCounters() {
    lastSystemTime = System.nanoTime();

    if (getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
      lastProcessCpuTime = ((OperatingSystemMXBean)getOperatingSystemMXBean()).getProcessCpuTime();
    }
  }

  public long getMemoryUsage() {
    long physicalMemorySize = -1L;
    if (getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
      physicalMemorySize = ((OperatingSystemMXBean)getOperatingSystemMXBean()).getFreePhysicalMemorySize();
    }
    return physicalMemorySize;
  }

  public String printGCStats() {
    long totalGarbageCollections = 0;
    long garbageCollectionTime = 0;

    for (java.lang.management.GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {

      long count = gc.getCollectionCount();

      if (count >= 0) {
        totalGarbageCollections += count;
      }

      long time = gc.getCollectionTime();

      if (time >= 0) {
        garbageCollectionTime += time;
      }
    }

    return "Total Garbage Collections: " + totalGarbageCollections + "\n" +
           "Total Garbage Collection Time (ms): " + garbageCollectionTime + "\n";

  }
}
