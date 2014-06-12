package org.rainfall.statistics;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsManager {

  private static final ConcurrentHashMap<String, StatisticsObserver> observers = new ConcurrentHashMap<String, StatisticsObserver>();

  public static <T extends Enum<T>> StatisticsObserver<T> getStatisticObserver(final String name, final Class<T> results) {
    observers.putIfAbsent(name, new StatisticsObserver<T>(results));
    return observers.get(name);
  }

  public static ConcurrentHashMap<String, StatisticsObserver> getStatisticObservers() {
    return observers;
  }
}
