package org.rainfall.reporting;

import org.rainfall.statistics.StatisticsObserver;

/**
 * A reporter is a class that will send the metrics to some output (text, file, etc.)
 *
 * @author Aurelien Broszniowski
 */

public interface Reporter<K extends Enum<K>> {

  void report(StatisticsObserver<K>  statisticsObserver);

}
