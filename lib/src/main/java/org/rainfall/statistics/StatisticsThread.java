package org.rainfall.statistics;

import org.rainfall.configuration.ReportingConfig;
import org.rainfall.Reporter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO : write html reporter and use reporter(s)
 *
 * @author Aurelien Broszniowski
 */

public class StatisticsThread<K extends Enum<K>> extends Thread {

  boolean stopped = false;
  private ReportingConfig reportingConfig;

  public StatisticsThread(final ReportingConfig reportingConfig) {
    this.reportingConfig = reportingConfig;
  }

  @Override
  public void run() {
    boolean noStatToReport = true;
    while (!stopped && noStatToReport) {
/*
      System.out.println("*** Displaying stats");

      ConcurrentHashMap<String, StatisticsObserver> statisticObservers = StatisticsObserversFactory.getInstance().getStatisticObservers();

      Set<Reporter> reporters = reportingConfig.getReporters();
      noStatToReport = true;
      for (StatisticsObserver observer : statisticObservers.values()) {
        List statistics = observer.peekAll();
        for (Reporter reporter : reporters) {
          reporter.report(statistics);
        }
        noStatToReport &= observer.hasEmptyQueue();
      }
      System.out.println("******");
*/

      try {
        sleep(1000);
      } catch (InterruptedException e) {
        this.stopped = true;
      }

    }
  }

  public void end() {
    System.out.println("*** Displaying stats");

    ConcurrentHashMap<String, StatisticsObserver> statisticObservers = StatisticsObserversFactory.getInstance().getStatisticObservers();

    Set<Reporter> reporters = reportingConfig.getReporters();
    for (StatisticsObserver observer : statisticObservers.values()) {
      List statistics = observer.peekAll();
      for (Reporter reporter : reporters) {
        reporter.report(statistics);
      }
    }
    System.out.println("******");


    this.stopped = true;
  }
}
