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
 * Hold the metrics for a statistic sampling
 *
 * @author Aurelien Broszniowski
 */
public class Metrics {

  private Long counter;
  private Double latency;

  public Metrics(final Long counter, final Double latency) {
    this.counter = counter;
    this.latency = latency;
  }

  public Long getCounter() {
    return counter;
  }

  public void setCounter(final Long counter) {
    this.counter = counter;
  }

  public Double getLatency() {
    return latency;
  }

  public void setLatency(final Double latency) {
    this.latency = latency;
  }
}
