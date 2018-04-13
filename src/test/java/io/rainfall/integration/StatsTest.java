/*
 * Copyright (c) 2014-2018 Aurélien Broszniowski
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

package io.rainfall.integration;

import io.rainfall.configuration.ReportingConfig;
import io.rainfall.statistics.RuntimeStatisticsHolder;
import io.rainfall.statistics.Statistics;
import io.rainfall.statistics.StatisticsThread;
import io.rainfall.utils.SystemTest;
import io.rainfall.utils.TopOfSecondTimer;
import org.HdrHistogram.Histogram;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.integration.StatsTest.StatsTestResult.RESULT;

/**
 * @author Aurelien Broszniowski
 */

@Category(SystemTest.class)
public class StatsTest {

  @Test
  @Ignore
  public void testStatsHolderOnly() {
    TopOfSecondTimer topOfSecondTimer = new TopOfSecondTimer();
    StatisticsThread<StatsTestResult> stats = null;

    ReportingConfig reportingConfig = ReportingConfig.report(StatsTestResult.class).log(text());

    RuntimeStatisticsHolder<StatsTestResult> statisticsHolder = new RuntimeStatisticsHolder<StatsTestResult>(
        reportingConfig.getResults(), reportingConfig.getResultsReported(),
        reportingConfig.getStatisticsCollectors()
    );

    String name = "MY_TEST";
    statisticsHolder.addStatistics(name, new Statistics<StatsTestResult>(name, statisticsHolder.getResults() ));


    stats = new StatisticsThread<StatsTestResult>(statisticsHolder, reportingConfig, Arrays.asList("Example Test"),
        reportingConfig.getStatisticsCollectors());
    TimeUnit reportIntervalUnit = reportingConfig.getReportTimeUnit();
    long reportIntervalMillis = reportIntervalUnit.toMillis(reportingConfig.getReportInterval());

    topOfSecondTimer.scheduleAtFixedRate(stats, reportIntervalMillis);


    Map<Long, String> pseudoCache = new HashMap<Long, String>();
    for (long i = 0; i < 3000000; i++) {
      long start = statisticsHolder.getTimeInNs();
      pseudoCache.put(i % 100000, UUID.randomUUID().toString());
      long end = statisticsHolder.getTimeInNs();

      statisticsHolder.record(name, end - start, RESULT);
    }

    topOfSecondTimer.cancel();

    Histogram histogram = statisticsHolder.fetchHistogram(RESULT);
    histogram.outputPercentileDistribution(System.out, 1.0);
  }

  enum StatsTestResult {
    RESULT
  }
}
