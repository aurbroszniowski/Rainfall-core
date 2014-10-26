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

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserversFactory {

  private static final StatisticsObserversFactory factory = new StatisticsObserversFactory();

  public static StatisticsObserversFactory getInstance() { return factory; }

  private StatisticsObserversFactory() {}

  private final ConcurrentHashMap<String, StatisticsObserver> observers = new ConcurrentHashMap<String, StatisticsObserver>();

  @SuppressWarnings("unchecked")
  public <K extends Enum<K>> StatisticsObserver<K> getStatisticObserver(final String name, final Class<K> results) {
    this.observers.putIfAbsent(name, new StatisticsObserver<K>(results));
    return observers.get(name);
  }

  public ConcurrentHashMap<String, StatisticsObserver> getStatisticObservers() {
    return this.observers;
  }
}
