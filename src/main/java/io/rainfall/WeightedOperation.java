package io.rainfall;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO : changer le weight system -> on a soit -1 = 1 thread, ou calculler weights  -> public Scenario exec(final WeightedOperation... operations) {
 * TODO : RuntimestatisticsHolder -> virer l'interface et ne pas faire de pre-run
 *
 * @author Aurelien Broszniowski
 */

public class WeightedOperation {

  private float weight = 1;
  private final Operation operation;
  private int weightInPercent;
  private List<String> description;
  private Execution.ExecutionState state;

  public WeightedOperation(final Double weight, final Operation operation) {
    this.weight = weight.floatValue();
    this.weightInPercent = (int)(100 * weight);
    //TODO : read annotation to add desc.
    this.operation = operation;
  }

  public WeightedOperation(final Operation operation) {
    this.weight = -1;
    this.operation = operation;
  }

  public float getWeight() {
    return weight;
  }

  public int getWeightInPercent() {
    return this.weightInPercent;
  }

  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add("Operation weight : " + this.weightInPercent + " % ");
    desc.addAll(this.operation.getDescription());
    return desc;
  }

  ;

  public Operation getOperation() {
    return operation;
  }

  public Execution.ExecutionState getExecutionState() {
    return state;
  }

  public void markExecutionState(Execution.ExecutionState state) {
    this.state = state;
  }
}
