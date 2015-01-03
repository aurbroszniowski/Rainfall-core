package io.rainfall.statistics;

/**
 * @author Aurelien Broszniowski
 */
public class FunctionExecutor<E extends Enum<E>> {

  private OperationFunction<E> task;

  public FunctionExecutor(OperationFunction<E> task) {
    this.task = task;
  }

  public E apply() throws Exception {
    return task.apply();
  }
}
