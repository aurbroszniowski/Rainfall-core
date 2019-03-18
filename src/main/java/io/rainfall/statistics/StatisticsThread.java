/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
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

import io.rainfall.configuration.ReportingConfig;
import io.rainfall.reporting.Reporter;
import io.rainfall.statistics.collector.StatisticsCollector;

import java.util.List;
import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsThread<E extends Enum<E>> {

  private RuntimeStatisticsHolder<E> statisticsHolder;
  private ReportingConfig<E> reportingConfig;

  public StatisticsThread(final RuntimeStatisticsHolder<E> statisticsHolder, final ReportingConfig<E> reportingConfig,
                          final List<String> description, final Set<StatisticsCollector> statisticsCollectors) {
    this.statisticsHolder = statisticsHolder;
    this.reportingConfig = reportingConfig;

    for (StatisticsCollector statisticsCollector : statisticsCollectors) {
      statisticsCollector.initialize();
    }

    Set<Reporter<E>> reporters = reportingConfig.getLogReporters();
    for (Reporter<E> reporter : reporters) {
      reporter.header(description);
    }
  }

  public StatisticsPeekHolder<E> shutdown() {
    StatisticsPeekHolder<E> peek = statisticsHolder.peek();

    for (StatisticsCollector statisticsCollector : reportingConfig.getStatisticsCollectors()) {
      statisticsCollector.terminate();
    }

    Set<Reporter<E>> reporters = reportingConfig.getLogReporters();
    for (Reporter<E> reporter : reporters) {
      reporter.summarize(statisticsHolder);
    }
    return peek;
  }
}
