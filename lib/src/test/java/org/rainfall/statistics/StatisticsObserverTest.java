package org.rainfall.statistics;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserverTest {

  @Test
  public void testCounterAndLatency() throws Exception {
    Task task = mock(Task.class);
    StatisticsObserver<Results> observer = spy(new StatisticsObserver<Results>(Results.class));
    long start = 55 * 1000000L;
    long okEnd = 125 * 1000000L;
    long koEnd = 140 * 1000000L;
    when(observer.getTime()).thenReturn(start, okEnd, start, koEnd);

    when(task.definition()).thenReturn(Results.OK, Results.KO);
    observer.measure((Task<Results>)task);
    observer.measure((Task<Results>)task);

    List<Statistics<Results>> statisticsList = observer.peekAll();
    assertThat(statisticsList.size(), is(equalTo(1)));
    Statistics<Results> resultsStatistics = statisticsList.get(0);
    long okLatency = okEnd - start;
    long koLatency = koEnd - start;
    double averageLatency = new Double(okLatency + koLatency) / 2 / 1000000L;
    assertThat(resultsStatistics.averageLatencyInMs(), is(equalTo(averageLatency)));
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
