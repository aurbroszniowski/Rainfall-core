package io.rainfall.statistics;

import io.rainfall.TestException;

import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */
public interface StatisticsObserversHolder<E extends Enum<E>> {

  Set<String> getStatisticObserverKeys();

  StatisticsObserver getStatisticObserver(String name);

  StatisticsObserver getTotalStatisticObserver();

  void measure(String name, Class<E> results, Task task) throws TestException;

}
