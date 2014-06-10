package org.rainfall;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the main class, defining the DSL of the test scenario
 *
 * @author Aurelien Broszniowski
 */

public class Scenario {

  private String name;
  private final List<Operation> operations = new LinkedList<Operation>();

  public Scenario(final String name) {
    this.name = name;
  }

  public Scenario exec(final Operation operation) {
    operations.add(operation);
    return this;
  }

  public static Scenario scenario(final String name) {
    return new Scenario(name);
  }

  public List<Operation> getOperations() {
    return operations;
  }

}
