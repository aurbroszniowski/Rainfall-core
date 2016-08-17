package io.rainfall.statistics.collector;

import io.rainfall.statistics.exporter.Exporter;

/**
 * Interface for additional collectors to report data (e.g. GC Collector)
 *
 * @author Aurelien Broszniowski
 */
public interface StatisticsCollector {

  void initialize();

  void terminate();

  Exporter peek();

  String getName();

}
