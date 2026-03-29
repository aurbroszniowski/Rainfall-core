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
    OK
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
}
