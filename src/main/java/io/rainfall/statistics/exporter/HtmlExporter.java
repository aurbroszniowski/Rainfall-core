package io.rainfall.statistics.exporter;

/**
 * @author Aurelien Broszniowski
 */
public interface HtmlExporter<E extends Enum<E>> extends Exporter {

  void ouputCsv(final String basedir) throws Exception;

  String outputHtml();

}
