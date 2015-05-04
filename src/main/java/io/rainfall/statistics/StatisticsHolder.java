package io.rainfall.statistics;

import io.rainfall.TestException;

import org.HdrHistogram.Histogram;

import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */
public interface StatisticsHolder<E extends Enum<E>> {

  Enum<E>[] getResultsReported();

  Set<String> getStatisticsKeys();

  Statistics<E> getStatistics(String name);

  Histogram getHistogram(Enum<E> result);

  void measure(String name, FunctionExecutor functionExecutor) throws TestException;

  void reset();

  long getCurrentTps(Enum result);
}
