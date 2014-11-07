/*
 * Copyright 2014 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rainfall;

/**
 * This is the Scenario runner
 * It takes the scenario(s), the configuration(s) and the assertion(s) and executes them
 *
 * @author Aurelien Broszniowski
 */

public class Runner {

  // add scenario beExecuted
  public static ScenarioRun setUp(Scenario scenario) {
    return new ScenarioRun(scenario);
  }

}
