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
