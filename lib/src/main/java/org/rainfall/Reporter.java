package org.rainfall;

import org.rainfall.statistics.Statistics;

import java.util.List;

/**
 * A reporter is a class that will send the metrics to some output (text, file, etc.)
 *
 * @author Aurelien Broszniowski
 */

public interface Reporter<K extends Enum<K>> {

  void report(List<Statistics<K>> statistics);

}
