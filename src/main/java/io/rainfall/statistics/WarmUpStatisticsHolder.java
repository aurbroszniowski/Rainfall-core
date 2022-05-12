/*
 * Copyright (c) 2014-2022 Aurélien Broszniowski
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

public class WarmUpStatisticsHolder extends StatisticsHolder {
  @Override
  public Enum[] getResultsReported() {
    return new Enum[0];
  }

  @Override
  public Set<String> getStatisticsKeys() {
    return null;
  }

  @Override
  public Statistics getStatistics(String name) {
    return null;
  }

  @Override
  public Set<StatisticsCollector> getStatisticsCollectors() {
    return null;
  }

  @Override
  public Histogram fetchHistogram(Enum result) {
    return null;
  }

  @Override
  public void reset() {

  }

  @Override
  public long getCurrentTps(Enum result) {
    return 0;
  }

  @Override
  public void record(String name, long responseTimeInNs, Enum result) {

  }

  @Override
  public void increaseAssertionsErrorsCount(String name) {

  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public long getStartTime() {
    return 0;
  }
}
