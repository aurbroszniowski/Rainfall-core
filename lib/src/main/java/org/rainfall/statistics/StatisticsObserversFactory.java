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
