package org.rainfall.statistics;

import org.rainfall.TestException;

import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */
public interface StatisticsObserversHolder {

  Set<String> getStatisticObserverKeys();

  StatisticsObserver getStatisticObserver(String name);

  StatisticsObserver getTotalStatisticObserver();

  void measure(String name, Result[] results, Task task) throws TestException;

}
