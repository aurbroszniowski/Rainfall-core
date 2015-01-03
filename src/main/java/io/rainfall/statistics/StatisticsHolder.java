package io.rainfall.statistics;

import io.rainfall.TestException;

import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */
public interface StatisticsHolder<E extends Enum<E>> {

  Set<String> getStatisticsKeys();

  Statistics<E> getStatistics(String name);

  void measure(String name, FunctionExecutor functionExecutor) throws TestException;

}
