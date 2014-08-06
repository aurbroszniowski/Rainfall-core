package org.rainfall.jcache.operation;

/**
 * Holds the weight of a specific {@link org.rainfall.Operation}, in order to calculate the statistic occurrence
 * when the operation should be executed
 *
 * @author Aurelien Broszniowski
 */

public class OperationWeight {

  public enum OPERATION {PUT, PUTIFABSENT}

  private double weight = 0.0;
  private OPERATION operation;

  public OperationWeight(final OPERATION operation, final double weight) {
    this.weight = weight;
    this.operation = operation;
  }

  public static OperationWeight operation(OPERATION operation, double weight) {
    return new OperationWeight(operation, weight);
  }

  public double getWeight() {
    return weight;
  }

  public OPERATION getOperation() {
    return operation;
  }
}
