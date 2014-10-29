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

import java.util.Calendar;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsThread extends TimerTask {

  private StatisticsObserversFactory observersFactory;
  private ReportingConfig reportingConfig;

  public StatisticsThread(final StatisticsObserversFactory observersFactory, final ReportingConfig reportingConfig) {
    this.observersFactory = observersFactory;
    this.reportingConfig = reportingConfig;
  }

  @Override
  @SuppressWarnings("unsigned")
  public void run() {
    ConcurrentHashMap<String, StatisticsObserver> statisticObservers = observersFactory.getStatisticObservers();

    Set<Reporter> reporters = reportingConfig.getReporters();
    for (StatisticsObserver observer : statisticObservers.values()) {
      StatisticsHolder holder = observer.peek();
      for (Reporter reporter : reporters) {
        reporter.report(holder);
      }
    }
  }
}
