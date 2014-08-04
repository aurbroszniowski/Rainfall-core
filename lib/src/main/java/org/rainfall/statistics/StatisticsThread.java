package org.rainfall.statistics;

import org.rainfall.configuration.ReportingConfig;
import org.rainfall.Reporter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO : write html reporter and use reporter(s)
 * @author Aurelien Broszniowski
 */

public class StatisticsThread extends Thread {

  boolean stopped = false;
  private ReportingConfig reportingConfig;

  public StatisticsThread(final ReportingConfig reportingConfig) {
    this.reportingConfig = reportingConfig;
  }

  @Override
  public void run() {
    while (!stopped) {
      ConcurrentHashMap<String, StatisticsObserver> statisticObservers = StatisticsManager.getStatisticObservers();
      Set<Reporter> reporters = reportingConfig.getReporters();

      for (StatisticsObserver observer : statisticObservers.values()) {
        for (Reporter reporter : reporters) {
          reporter.report(observer);
        }
      }
      try {
        sleep(1000);
      } catch (InterruptedException e) {
        this.stopped = true;
      }
    }
  }

  public void end() {
    this.stopped = true;
  }
}
