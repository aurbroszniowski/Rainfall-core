package io.rainfall.statistics;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Aurelien Broszniowski
 */
public class StatisticsTest {

  @Test
  public void testAverageLatency() {
    Result one = new Result("ONE");
    Result two = new Result("TWO");
    Result three = new Result("THREE");
    final Result[] keys = new Result[] { one, two, three };
    Statistics statistics = new Statistics(keys);

    statistics.increaseCounterAndSetLatency(two, 10.5d);
    statistics.increaseCounterAndSetLatency(two, 3.4d);
    statistics.increaseCounterAndSetLatency(two, 9.7d);

    assertThat(statistics.getAverageLatency(two), is(((10.5d + 3.4d + 9.7d) / 3)));
  }

  @Test
  public void testTotalAverageLatency() {
    Result one = new Result("ONE");
    Result two = new Result("TWO");
    Result three = new Result("THREE");
    final Result[] keys = new Result[] { one, two, three };
    Statistics statistics = new Statistics(keys);

    statistics.increaseCounterAndSetLatency(two, 10.5d);
    statistics.increaseCounterAndSetLatency(two, 3.4d);
    statistics.increaseCounterAndSetLatency(two, 9.7d);
    statistics.increaseCounterAndSetLatency(one, 4.3d);
    statistics.increaseCounterAndSetLatency(one, 6.3d);
    statistics.increaseCounterAndSetLatency(two, 11.3d);
    statistics.increaseCounterAndSetLatency(two, 6.2d);

    assertThat(statistics.averageLatencyInMs(), is((
        ((10.5d + 3.4d + 9.7d + 11.3d + 6.2d) / 5) + ((4.3d + 6.3d) / 2)) / 2
    ));
  }

  @Test
  public void testCounter() {
    Result one = new Result("ONE");
    Result two = new Result("TWO");
    Result three = new Result("THREE");
    final Result[] keys = new Result[] { one, two, three };

    Statistics statistics = new Statistics(keys);

    statistics.increaseCounterAndSetLatency(two, 10.5d);
    statistics.increaseCounterAndSetLatency(two, 3.4d);
    statistics.increaseCounterAndSetLatency(two, 9.7d);

    assertThat(statistics.getCounter(two), is(3L));
  }

  @Test
  public void testSumOfCounters() {
    Result one = new Result("ONE");
    Result two = new Result("TWO");
    Result three = new Result("THREE");
    final Result[] keys = new Result[] { one, two, three };

    Statistics statistics = new Statistics(keys);

    statistics.increaseCounterAndSetLatency(one, 10.5d);
    statistics.increaseCounterAndSetLatency(two, 3.4d);
    statistics.increaseCounterAndSetLatency(two, 9.7d);
    statistics.increaseCounterAndSetLatency(three, 9.7d);

    assertThat(statistics.sumOfCounters(), is(4L));
  }

  @Test
  public void testCounterMultiple() {
    Result one = new Result("ONE");
    Result two = new Result("TWO");
    Result three = new Result("THREE");
    final Result[] keys = new Result[] { one, two, three };

    Statistics statistics = new Statistics(keys);

    statistics.increaseCounterAndSetLatency(two, 10.5d);
    statistics.increaseCounterAndSetLatency(three, 3.4d);
    statistics.increaseCounterAndSetLatency(two, 3.4d);
    statistics.increaseCounterAndSetLatency(two, 9.7d);
    statistics.increaseCounterAndSetLatency(one, 9.7d);
    statistics.increaseCounterAndSetLatency(three, 9.7d);

    assertThat(statistics.getCounter(one), is(1L));
    assertThat(statistics.getCounter(two), is(3L));
    assertThat(statistics.getCounter(three), is(2L));
  }

  @Test
  public void testTps() {
    Result one = new Result("ONE");
    Result two = new Result("TWO");
    Result three = new Result("THREE");
    final Result[] keys = new Result[] { one, two, three };

    Statistics statistics = spy(new Statistics(keys));
    long startTime = 12 * 1000000L;
    long endTime = 15124 * 1000000L;
    statistics.setStartTime(startTime);
    when(statistics.getTime()).thenReturn(endTime);

    for (int i = 0; i < 100; i++)
      statistics.increaseCounterAndSetLatency(two, 50.5d);

    long length = (15124 - 12) * 1000000L;
    long tps = 100 / (length / 1000000L / 1000L);

    assertThat(statistics.getTps(two), is(tps));
  }

  @Test
  public void testTotalTps() {
    Result one = new Result("ONE");
    Result two = new Result("TWO");
    Result three = new Result("THREE");
    final Result[] keys = new Result[] { one, two, three };

    Statistics statistics = spy(new Statistics(keys));
    long startTime = 12 * 1000000L;
    long endTime = 15124 * 1000000L;
    statistics.setStartTime(startTime);
    when(statistics.getTime()).thenReturn(endTime);

    for (int i = 0; i < 100; i++) {
      statistics.increaseCounterAndSetLatency(one, 50.5d);
      statistics.increaseCounterAndSetLatency(two, 50.5d);
      statistics.increaseCounterAndSetLatency(three, 50.5d);
    }

    long length = (15124 - 12) * 1000000L;
    long tps = 3 * 100 / (length / 1000000L / 1000L);

    assertThat(statistics.averageTps(), is(tps));
  }

}
