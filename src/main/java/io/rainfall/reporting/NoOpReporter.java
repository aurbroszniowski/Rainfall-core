package io.rainfall.reporting;

import io.rainfall.Reporter;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.StatisticsPeekHolder;

/**
 * A Reporter that does nothing
 * @author yizhang
 *
 */
public class NoOpReporter extends Reporter {

  @Override
  public void report(StatisticsPeekHolder statisticsHolder) {
    // TODO Auto-generated method stub
  }

  @Override
  public void summarize(StatisticsHolder statisticsHolder) {
    // TODO Auto-generated method stub
  }

}
