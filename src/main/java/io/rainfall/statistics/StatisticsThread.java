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

package io.rainfall.statistics;

import io.rainfall.Reporter;
import io.rainfall.configuration.ReportingConfig;

import java.util.List;
import java.util.Set;
import java.util.TimerTask;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsThread<E extends Enum<E>> extends TimerTask {

  private RuntimeStatisticsHolder<E> statisticsHolder;
  private ReportingConfig<E> reportingConfig;
  private List<String> description;

  public StatisticsThread(final RuntimeStatisticsHolder<E> statisticsHolder, final ReportingConfig<E> reportingConfig,
                          final List<String> description) {
    this.description = description;
    Thread.currentThread().setName("Rainfall-core Statistics Thread");
    this.statisticsHolder = statisticsHolder;
    this.reportingConfig = reportingConfig;

    Set<Reporter<E>> reporters = reportingConfig.getLogReporters();
    for (Reporter<E> reporter : reporters) {
      reporter.header(description);
    }
  }

  @Override
  public void run() {
    StatisticsPeekHolder<E> peek = statisticsHolder.peek();
    Set<Reporter<E>> reporters = reportingConfig.getLogReporters();
    for (Reporter<E> reporter : reporters) {
      reporter.report(peek);
    }
  }

  public StatisticsPeekHolder<E> stop() {
    StatisticsPeekHolder<E> peek = statisticsHolder.peek();
    Set<Reporter<E>> reporters = reportingConfig.getLogReporters();
    for (Reporter<E> reporter : reporters) {
      reporter.summarize(statisticsHolder);
    }
    super.cancel();
    return peek;
  }
}
