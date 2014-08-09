package org.rainfall;

import org.rainfall.statistics.StatisticsObserver;

/**
 * A reporter is a class that will send the metrics to some output (text, file, etc.)
 *
 * @author Aurelien Broszniowski
 */

public interface Reporter {

  void report(StatisticsObserver statisticsObserver);

}
