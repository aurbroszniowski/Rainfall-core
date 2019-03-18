package io.rainfall.reporting;

import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeekHolder;

import java.util.List;


public class PeriodicReporter<E extends Enum<E>> implements Reporter<E> {
  private final long reportingIntervalInMillis;
  private final Reporter<E> reporter;

  public PeriodicReporter(Reporter<E> reporter, long reportingIntervalInMillis) {
    this.reporter = reporter;
    this.reportingIntervalInMillis = reportingIntervalInMillis;
  }

  @Override
  public void header(List<String> description) {
    reporter.header(description);
  }

  @Override
  public void report(StatisticsPeekHolder<E> statisticsHolder) {
    reporter.report(statisticsHolder);
  }

  @Override
  public void summarize(StatisticsHolder<E> statisticsHolder) {
    reporter.summarize(statisticsHolder);
  }

  public long getReportingIntervalInMillis() {
    return reportingIntervalInMillis;
  }
}
