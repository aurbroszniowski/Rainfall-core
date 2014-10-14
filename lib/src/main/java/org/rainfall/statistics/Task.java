package org.rainfall.statistics;

/**
 * @author Aurelien Broszniowski
 */

public abstract class Task <K> {
  public abstract K definition() throws Exception;
}
