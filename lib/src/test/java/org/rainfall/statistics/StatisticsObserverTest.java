package org.rainfall.statistics;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserverTest {

  @Test
  public void testMinLatency() {
    StatisticsObserver observer = spy(new StatisticsObserver<Results>(Results.class));
    when(observer.getTime()).thenReturn(10 * 1000000L, 20 * 1000000L, 30 * 1000000L);
    long start = observer.start();
    observer.end(start, Results.OK); // 20 - 10   = 10
    observer.end(start, Results.KO); // 30 - 10   = 20
    assertThat(observer.getMinLatency(Results.OK), is(equalTo(10L)));
    assertThat(observer.getMinLatency(Results.KO), is(equalTo(20L)));
  }

  //TODO test minLatency
  //TODO test maxLatency
  //TODO test averageLatency
  //TODO statisticsMap merge


  public enum Results {
    OK, KO
  }
}
