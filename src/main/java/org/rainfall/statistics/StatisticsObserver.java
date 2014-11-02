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

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserver {

  private final Result[] keys;
  private final Statistics statistics;
  private long timestamp;
  private Long previousCounter = 0L;

  public StatisticsObserver(Result[] results) {
    this.keys = results;
    this.statistics = new Statistics(results);
  }

  public Result[] getKeys() {
    return keys;
  }

  public StatisticsHolder peek() {
    //TODO : is instantiation needed?
    return new StatisticsHolder(this.timestamp, statistics);
  }

  public boolean hasEmptyQueue() {
    if (this.previousCounter.equals(this.statistics.sumOfCounters())) {
      return true;
    }
    this.previousCounter = this.statistics.sumOfCounters();
    return false;
  }

  public void setTimestamp(final long timestamp) {
    this.timestamp = timestamp;
  }

  public Statistics getStatistics() {
    return statistics;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
