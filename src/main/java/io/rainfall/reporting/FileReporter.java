package io.rainfall.reporting;

import java.io.File;

/**
 * @author Aurelien Broszniowski
 */

public abstract class FileReporter<E extends Enum<E>>  extends Reporter<E> {

  protected File reportPath;

  public File getReportPath() {
    return reportPath;
  }
}
