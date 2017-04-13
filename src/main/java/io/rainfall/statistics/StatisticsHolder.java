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

import io.rainfall.statistics.collector.StatisticsCollector;
import org.HdrHistogram.Histogram;

import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */
public abstract class StatisticsHolder<E extends Enum<E>> {

  public abstract Enum<E>[] getResultsReported();

  public abstract Set<String> getStatisticsKeys();

  public abstract Statistics<E> getStatistics(String name);

  public abstract Set<StatisticsCollector> getStatisticsCollectors();

  public abstract Histogram fetchHistogram(final Enum<E> result);

  public abstract void reset();

  public abstract long getCurrentTps(Enum result);

  public abstract void record(String name, long responseTimeInNs, Enum result);

  public abstract void increaseAssertionsErrorsCount(String name);

  public long getTimeInNs() {
    return System.nanoTime();
  }

}
