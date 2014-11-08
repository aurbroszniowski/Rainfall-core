package io.rainfall.statistics;

import io.rainfall.TestException;

import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */
public class InitStatisticsObserversHolder<E extends Enum<E>> implements StatisticsObserversHolder<E> {

  private RuntimeStatisticsObserversHolder<E> observersFactory;

  public InitStatisticsObserversHolder(RuntimeStatisticsObserversHolder<E> observersFactory) {
    this.observersFactory = observersFactory;
  }

  @Override
  public Set<String> getStatisticObserverKeys() {
    throw new UnsupportedOperationException("Should not be implemented");
  }

  @Override
  public StatisticsObserver getStatisticObserver(final String name) {
    throw new UnsupportedOperationException("Should not be implemented");
  }

  @Override
  public StatisticsObserver getTotalStatisticObserver() {
    throw new UnsupportedOperationException("Should not be implemented");
  }

  @Override
  public void measure(final String name, final Class<E> results, final Task task) throws TestException {
    observersFactory.addStatisticsObserver(name, new StatisticsObserver<E>(results));
  }
}
