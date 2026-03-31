/*
 * Copyright (c) 2026 Aurélien Broszniowski
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

import org.HdrHistogram.Histogram;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RuntimeStatisticsHolderTest {

  private enum Result {
    OK,
    ERROR,
    TIMEOUT
  }

  @Test
  public void getCurrentTpsShouldReturnZeroWhenNoStatisticsExist() {
    RuntimeStatisticsHolder<Result> holder = new RuntimeStatisticsHolder<Result>(
        Result.values(), Result.values(), Collections.emptySet());

    assertThat(holder.getCurrentTps(Result.OK), is(0L));
  }

  @Test
  public void increaseAssertionsErrorsCountShouldInitializeCounters() {
    RuntimeStatisticsHolder<Result> holder = new RuntimeStatisticsHolder<Result>(
        Result.values(), Result.values(), Collections.emptySet());

    holder.increaseAssertionsErrorsCount("op");

    StatisticsPeekHolder<Result> peekHolder = holder.peek();
    assertThat(peekHolder.getAssertionsErrorsCount("op"), is(1L));
    assertThat(peekHolder.getTotalAssertionsErrorsCount(), is(1L));
  }

  @Test
  public void histogramsShouldBeIsolatedPerStatisticsHolder() {
    RuntimeStatisticsHolder<Result> firstHolder = new RuntimeStatisticsHolder<Result>(
        Result.values(), Result.values(), Collections.emptySet());
    RuntimeStatisticsHolder<Result> secondHolder = new RuntimeStatisticsHolder<Result>(
        Result.values(), Result.values(), Collections.emptySet());

    firstHolder.record("op", 10L, Result.OK);
    secondHolder.record("op", 20L, Result.OK);

    assertThat(firstHolder.fetchHistogram(Result.OK).getTotalCount(), is(1L));
    assertThat(secondHolder.fetchHistogram(Result.OK).getTotalCount(), is(1L));
  }

  @Test
  public void fetchHistogramShouldReuseTheAggregateHistogramInstance() {
    RuntimeStatisticsHolder<Result> holder = new RuntimeStatisticsHolder<Result>(
        Result.values(), Result.values(), Collections.emptySet());

    holder.record("op", 10L, Result.OK);

    Histogram firstFetch = holder.fetchHistogram(Result.OK);
    Histogram secondFetch = holder.fetchHistogram(Result.OK);

    assertThat(firstFetch, is(secondFetch));
    assertThat(secondFetch.getTotalCount(), is(1L));
  }

  @Test
  public void peekShouldAggregateAverageLatenciesUsingRawTotals() {
    RuntimeStatisticsHolder<Result> holder = new RuntimeStatisticsHolder<Result>(
        Result.values(), Result.values(), Collections.emptySet());

    holder.record("fast", 1_000_000L, Result.OK);
    holder.record("slow", 10_000_000L, Result.OK);
    for (int i = 0; i < 8; i++) {
      holder.record("slow", 1_000_000L, Result.OK);
    }

    StatisticsPeekHolder<Result> peekHolder = holder.peek();

    assertThat(peekHolder.getTotalStatisticsPeeks().getSumOfPeriodicCounters(), is(10L));
    assertThat(peekHolder.getTotalStatisticsPeeks().getAverageOfPeriodicAverageLatencies(), is(1.9d));
    assertThat(peekHolder.getTotalStatisticsPeeks().getAverageOfCumulativeAverageLatencies(), is(1.9d));
    assertThat(peekHolder.getTotalStatisticsPeeks().getPeriodicAverageLatencyInMs(Result.OK), is(1.9d));
    assertThat(peekHolder.getTotalStatisticsPeeks().getCumulativeAverageLatencyInMs(Result.OK), is(1.9d));
  }

  @Test
  public void peekShouldAggregateSubsetOfReportedResultsByEnumKey() {
    RuntimeStatisticsHolder<Result> holder = new RuntimeStatisticsHolder<Result>(
        Result.values(), new Result[]{Result.ERROR}, Collections.emptySet());

    holder.record("op", 1_000_000L, Result.OK);
    holder.record("op", 1_000_000L, Result.OK);
    for (int i = 0; i < 5; i++) {
      holder.record("op", 2_000_000L, Result.ERROR);
    }

    StatisticsPeek<Result> total = holder.peek().getTotalStatisticsPeeks();

    assertThat(total.getSumOfPeriodicCounters(), is(5L));
    assertThat(total.getSumOfCumulativeCounters(), is(5L));
    assertThat(total.getPeriodicCounters(Result.ERROR), is(5L));
    assertThat(total.getCumulativeCounters(Result.ERROR), is(5L));
    assertThat(total.getPeriodicAverageLatencyInMs(Result.ERROR), is(2.0d));
    assertThat(total.getCumulativeAverageLatencyInMs(Result.ERROR), is(2.0d));
  }

  @Test
  public void peekShouldAggregateReorderedReportedResultsByEnumKey() {
    RuntimeStatisticsHolder<Result> holder = new RuntimeStatisticsHolder<Result>(
        Result.values(), new Result[]{Result.TIMEOUT, Result.OK}, Collections.emptySet());

    holder.record("op", 3_000_000L, Result.TIMEOUT);
    holder.record("op", 3_000_000L, Result.TIMEOUT);
    holder.record("op", 1_000_000L, Result.OK);

    StatisticsPeek<Result> total = holder.peek().getTotalStatisticsPeeks();

    assertThat(total.getSumOfPeriodicCounters(), is(3L));
    assertThat(total.getPeriodicCounters(Result.TIMEOUT), is(2L));
    assertThat(total.getPeriodicCounters(Result.OK), is(1L));
    assertThat(total.getPeriodicAverageLatencyInMs(Result.TIMEOUT), is(3.0d));
    assertThat(total.getPeriodicAverageLatencyInMs(Result.OK), is(1.0d));
  }
}
