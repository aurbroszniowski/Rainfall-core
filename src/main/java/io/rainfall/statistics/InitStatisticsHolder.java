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

import io.rainfall.TestException;
import org.HdrHistogram.Histogram;

import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */
public class InitStatisticsHolder<E extends Enum<E>> implements StatisticsHolder<E> {

  private RuntimeStatisticsHolder<E> statisticsHolder;

  public InitStatisticsHolder(RuntimeStatisticsHolder<E> statisticsHolder) {
    this.statisticsHolder = statisticsHolder;
  }

  @Override
  public Enum<E>[] getResultsReported() {
    throw new UnsupportedOperationException("Should not be implemented");
  }

  @Override
  public Set<String> getStatisticsKeys() {
    throw new UnsupportedOperationException("Should not be implemented");
  }

  @Override
  public Statistics<E> getStatistics(final String name) {
    throw new UnsupportedOperationException("Should not be implemented");
  }

  @Override
  public Histogram getHistogram(final Enum<E> result) {
    throw new UnsupportedOperationException("Should not be implemented");
  }

  @Override
  public void reset() {
    statisticsHolder.reset();
  }

  @Override
  public long getCurrentTps(Enum result) {
    return 1L;
  }

  @Override
  public void record(final String name, final long responseTime, final Enum result) {
    statisticsHolder.addStatistics(name, new Statistics<E>(name, statisticsHolder.getResults()));
  }
}
