package org.rainfall.statistics;

import jsr166e.ConcurrentHashMapV8;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserverTest {

  @Test
  public void testCounterAndLatency() throws Exception {
    Task task = mock(Task.class);
    StatisticsObserver<Results> observer = spy(new StatisticsObserver<Results>(Results.class));
    long start = 10 * 1000000L;
    long okEnd = 25 * 1000000L;
    long koEnd = 40 * 1000000L;
    when(observer.getTime()).thenReturn(start, okEnd, start, koEnd);

    when(task.definition()).thenReturn(Results.OK, Results.KO);
    observer.measure((Task<Results>)task);
    observer.measure((Task<Results>)task);

    ConcurrentHashMapV8<Long, Statistics<Results>> stats = observer.getStatisticsMap();
    Statistics<Results> resultsStatistics = stats.get(start);
    long okLatency = okEnd - start;
    long koLatency = koEnd - start;
    double averageLatency = (okLatency + koLatency) / 2;
    assertThat(resultsStatistics.averageLatency(), is(equalTo(averageLatency)));
    assertThat(resultsStatistics.sumOfCounters(), is(equalTo(2L)));
    assertThat(resultsStatistics.getLatency().get(Results.OK), is(equalTo(okLatency)));
    assertThat(resultsStatistics.getCounter().get(Results.OK), is(equalTo(1L)));
    assertThat(resultsStatistics.getLatency().get(Results.KO), is(equalTo(koLatency)));
    assertThat(resultsStatistics.getCounter().get(Results.KO), is(equalTo(1L)));
  }

  //TODO statisticsMap merge


  public enum Results {
    OK, KO
  }
}
