package io.rainfall.statistics;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * @author Aurelien Broszniowski
 */
public class StatisticsTest {

  @Test
  public void testPeriodicCounters() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    assertThat(statistics.getPeriodicCounters(two).longValue(), is(3L));
    assertThat(statistics.getPeriodicTotalLatenciesInNs(two).longValue(), is((105 + 34 + 97) * 1000000L));
  }

  @Test
  public void testPeriodicCountersAreResetAfterPeek() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    assertThat(statistics.getPeriodicCounters(two).longValue(), is(not(0L)));
    assertThat(statistics.getPeriodicTotalLatenciesInNs(two).doubleValue(), is(not(0d)));
    statistics.peek(1L);
    assertThat(statistics.getPeriodicCounters(two).longValue(), is(0L));
    assertThat(statistics.getPeriodicTotalLatenciesInNs(two).doubleValue(), is(0d));
  }

  @Test
  public void testCumulativeCounters() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    assertThat(statistics.getCumulativeCounters(two).longValue(), is(0L));
    assertThat(statistics.getCumulativeTotalLatencies(two).longValue(), is(0L));
    statistics.peek(1L);
    assertThat(statistics.getCumulativeCounters(two).longValue(), is(3L));
    assertThat(statistics.getCumulativeTotalLatencies(two).longValue(), is((105 + 34 + 97) * 1000000L));
  }

/*
TODO : fix
  @Test
  public void testTotalAverageLatency() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>(keys);

    statistics.increaseCounterAndSetLatencyInNs(two, 10.5d * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 3.4d * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 9.7d * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(one, 4.3d * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(one, 6.3d * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 11.3d * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 6.2d * 1000000L);

    assertThat(statistics.totalAverageLatencyInMs(), is(
        new BigDecimal((((10.5d + 3.4d + 9.7d + 11.3d + 6.2d) / 5) + ((4.3d + 6.3d) / 2)) / 2).setScale(2, BigDecimal.ROUND_FLOOR)
            .doubleValue()
    ));
  }

  @Test
  public void testCounter() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };

    Statistics<Result> statistics = new Statistics<Result>(keys);

    statistics.increaseCounterAndSetLatencyInNs(two, 10.5d);
    statistics.increaseCounterAndSetLatencyInNs(two, 3.4d);
    statistics.increaseCounterAndSetLatencyInNs(two, 9.7d);

    assertThat(statistics.getCounter(two), is(3L));
  }

  @Test
  public void testSumOfCounters() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };

    Statistics<Result> statistics = new Statistics<Result>(keys);

    statistics.increaseCounterAndSetLatencyInNs(one, 10.5d);
    statistics.increaseCounterAndSetLatencyInNs(two, 3.4d);
    statistics.increaseCounterAndSetLatencyInNs(two, 9.7d);
    statistics.increaseCounterAndSetLatencyInNs(three, 9.7d);

    assertThat(statistics.sumOfCounters(), is(4L));
  }

  @Test
  public void testCounterMultiple() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };

    Statistics<Result> statistics = new Statistics<Result>(keys);

    statistics.increaseCounterAndSetLatencyInNs(two, 10.5d);
    statistics.increaseCounterAndSetLatencyInNs(three, 3.4d);
    statistics.increaseCounterAndSetLatencyInNs(two, 3.4d);
    statistics.increaseCounterAndSetLatencyInNs(two, 9.7d);
    statistics.increaseCounterAndSetLatencyInNs(one, 9.7d);
    statistics.increaseCounterAndSetLatencyInNs(three, 9.7d);

    assertThat(statistics.getCounter(one), is(1L));
    assertThat(statistics.getCounter(two), is(3L));
    assertThat(statistics.getCounter(three), is(2L));
  }

  @Test
  public void testTps() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };

    long startTime = 12 * 1000000L;
    long endTime = 15124 * 1000000L;
    Statistics<Result> statistics = spy(new Statistics<Result>(keys, startTime));
    when(statistics.getTimeInNs()).thenReturn(endTime);

    for (int i = 0; i < 100; i++)
      statistics.increaseCounterAndSetLatencyInNs(two, 50.5d);

    long length = (15124 - 12) * 1000000L;
    long tps = 100 / (length / 1000000L / 1000L);

    assertThat(statistics.getTps(two), is(tps));
  }

  @Test
  public void testTotalTps() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };

    long startTime = 12 * 1000000L;
    long endTime = 15124 * 1000000L;
    Statistics<Result> statistics = spy(new Statistics<Result>(keys, startTime));
    when(statistics.getTimeInNs()).thenReturn(endTime);

    for (int i = 0; i < 100; i++) {
      statistics.increaseCounterAndSetLatencyInNs(one, 50.5d);
      statistics.increaseCounterAndSetLatencyInNs(two, 50.5d);
      statistics.increaseCounterAndSetLatencyInNs(three, 50.5d);
    }

    long length = (15124 - 12) * 1000000L;
    long tps = 3 * 100 / (length / 1000000L / 1000L);

    assertThat(statistics.averageTps(), is(tps));
  }
*/

  enum Result {ONE, TWO, THREE}
}
