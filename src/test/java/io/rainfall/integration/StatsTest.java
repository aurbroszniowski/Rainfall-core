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

package io.rainfall.integration;

import io.rainfall.configuration.ReportingConfig;
import io.rainfall.reporting.Reporter;
import io.rainfall.statistics.RuntimeStatisticsHolder;
import io.rainfall.utils.SystemTest;
import org.HdrHistogram.Histogram;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    ScheduledExecutorService topOfSecondExecutor = Executors.newSingleThreadScheduledExecutor();

    final Reporter reporter = text();
    final ReportingConfig reportingConfig = ReportingConfig.report(StatsTestResult.class).log(reporter);

    final RuntimeStatisticsHolder<StatsTestResult> statisticsHolder = new RuntimeStatisticsHolder<StatsTestResult>(
        reportingConfig.getResults(), reportingConfig.getResultsReported(),
        reportingConfig.getStatisticsCollectors()
    );

    String name = "MY_TEST";

    TimeUnit reportIntervalUnit = reportingConfig.getReportTimeUnit();
    long reportIntervalMillis = reportIntervalUnit.toMillis(reportingConfig.getReportInterval());

    Calendar myDate = Calendar.getInstance();
    myDate.add(Calendar.SECOND, 1);
    myDate.set(Calendar.MILLISECOND, 0);
    Date afterOneSecond = myDate.getTime();
    long delay = afterOneSecond.getTime() - System.currentTimeMillis() - 4;
    topOfSecondExecutor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        reporter.report(statisticsHolder.peek());
      }
    }, delay, reportIntervalMillis, TimeUnit.MILLISECONDS);

    Map<Long, String> pseudoCache = new HashMap<Long, String>();
    for (long i = 0; i < 3000000; i++) {
      long start = statisticsHolder.getTimeInNs();
      pseudoCache.put(i % 100000, UUID.randomUUID().toString());
      long end = statisticsHolder.getTimeInNs();

      statisticsHolder.record(name, end - start, RESULT);
    }

    topOfSecondExecutor.shutdown();

    Histogram histogram = statisticsHolder.fetchHistogram(RESULT);
    histogram.outputPercentileDistribution(System.out, 1.0);
  }

  enum StatsTestResult {
    RESULT
  }

  @Test
  @Ignore
  public void testTopOfSecondExecutor() throws InterruptedException {
    final SimpleDateFormat sdfDate = new SimpleDateFormat(" HH:mm:ss.SSS");
    Thread t = new Thread() {
      @Override
      public void run() {
        System.out.println(sdfDate.format(new Date()));
      }
    };

    ScheduledExecutorService topOfSecondExecutor = Executors.newSingleThreadScheduledExecutor();
    Calendar myDate = Calendar.getInstance();
    myDate.add(Calendar.SECOND, 1);
    myDate.set(Calendar.MILLISECOND, 0);
    Date afterOneSecond = myDate.getTime();
    long delay = afterOneSecond.getTime() - System.currentTimeMillis() - 4;
    System.out.println(delay);
    topOfSecondExecutor.scheduleAtFixedRate(t, delay, 1000, TimeUnit.MILLISECONDS);
    Thread.sleep(10000);
    topOfSecondExecutor.shutdown();
  }
}
