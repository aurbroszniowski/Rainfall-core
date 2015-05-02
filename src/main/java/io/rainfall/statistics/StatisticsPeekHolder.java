package io.rainfall.statistics;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */
public class StatisticsPeekHolder<E extends Enum<E>> {
  public final static String ALL = "ALL";
  private final Enum<E>[] resultsReported;

  private Map<String, StatisticsPeek<E>> statisticsPeeks = new ConcurrentHashMap<String, StatisticsPeek<E>>();
  private StatisticsPeek<E> totalStatisticsPeeks = null;
  private long timestamp;

  public StatisticsPeekHolder(final Enum<E>[] resultsReported, final Map<String, Statistics<E>> statisticsMap) {
    this.resultsReported = resultsReported;
    this.timestamp = System.currentTimeMillis();
    for (String name : statisticsMap.keySet()) {
      statisticsPeeks.put(name, statisticsMap.get(name).peek(timestamp));
    }
    this.totalStatisticsPeeks = new StatisticsPeek<E>(ALL, this.resultsReported, this.timestamp);
    totalStatisticsPeeks.addAll(statisticsPeeks);
  }

  public StatisticsPeek<E> getStatisticsPeeks(String name) {
    return statisticsPeeks.get(name);
  }

  public Set<String> getStatisticsPeeksNames() {
    return statisticsPeeks.keySet();
  }

  public StatisticsPeek<E> getTotalStatisticsPeeks() {
    return totalStatisticsPeeks;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Enum<E>[] getResultsReported() {
    return resultsReported;
  }
}
