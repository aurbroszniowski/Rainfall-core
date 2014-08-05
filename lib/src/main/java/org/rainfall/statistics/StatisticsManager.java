package org.rainfall.statistics;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsManager {

  private static final ConcurrentHashMap<String, StatisticsObserver> observers = new ConcurrentHashMap<String, StatisticsObserver>();

  public static <K extends Enum<K>> StatisticsObserver<K> getStatisticObserver(final String name, final Class<K> results) {
    observers.putIfAbsent(name, new StatisticsObserver<K>(results));
    return observers.get(name);
  }

  public static ConcurrentHashMap<String, StatisticsObserver> getStatisticObservers() {
    return observers;
  }
}
