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

import org.rainfall.Reporter;
import org.rainfall.configuration.ReportingConfig;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO : write html reporter and use reporter(s)
 *
 * @author Aurelien Broszniowski
 */

public class StatisticsThread<K extends Enum<K>> extends Thread {

  boolean stopped = false;
  private ReportingConfig<K> reportingConfig;

  public StatisticsThread(final ReportingConfig<K> reportingConfig) {
    this.reportingConfig = reportingConfig;
  }

  @Override
  @SuppressWarnings("unsigned")
  public void run() {
    boolean statToReport = true;
    if (reportingConfig == null) {
      stopped = true;
    }
    while (!stopped && statToReport) {
      ConcurrentHashMap<String, StatisticsObserver> statisticObservers =
          StatisticsObserversFactory.getInstance().getStatisticObservers();

      Set<Reporter<K>> reporters = reportingConfig.getReporters();
      statToReport = true;
      for (StatisticsObserver<K> observer : statisticObservers.values()) {
        StatisticsHolder<K> holder = observer.peek();
        for (Reporter<K> reporter : reporters) {
          reporter.report(holder);
        }
//        noStatToReport &= observer.hasEmptyQueue();
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
