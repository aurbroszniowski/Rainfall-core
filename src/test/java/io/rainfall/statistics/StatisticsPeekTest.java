package io.rainfall.statistics;

import jsr166e.DoubleAdder;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Aurelien Broszniowski
 */
public class StatisticsPeekTest {

  @Test
  public void periodicAverageLatency() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    StatisticsPeek<Result> peek = statistics.peek(5L);

    assertThat(peek.getPeriodicAverageLatencyInMs(two), is((105d + 34d + 97d) / 3));
  }

  @Test
  public void cumulativeAverageLatency() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    StatisticsPeek<Result> peek = statistics.peek(5L);

    assertThat(peek.getCumulativeAverageLatencyInMs(two), is((105d + 34d + 97d) / 3));
  }

  @Test
  public void periodicAverageLatencyAfterTwoPeeks() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    statistics.peek(5L);

    statistics.increaseCounterAndSetLatencyInNs(two, 25 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 94 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 37 * 1000000L);

    StatisticsPeek<Result> peek = statistics.peek(8L);

    assertThat(peek.getPeriodicAverageLatencyInMs(two), is((25d + 94d + 37d) / 3));
  }

  @Test
  public void cumulativeAverageLatencyAfterTwoPeeks() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    statistics.peek(5L);

    statistics.increaseCounterAndSetLatencyInNs(two, 25 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 94 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 37 * 1000000L);

    StatisticsPeek<Result> peek = statistics.peek(8L);

    assertThat(round(peek.getCumulativeAverageLatencyInMs(two)), is(round((105d + 34d + 97d + 25d + 94d + 37d) / 6)));
  }

  private Double round(final Double value) {
    return Math.round(value * 10000.0) / 10000.0;
  }


  enum Result {ONE, TWO, THREE}
}
