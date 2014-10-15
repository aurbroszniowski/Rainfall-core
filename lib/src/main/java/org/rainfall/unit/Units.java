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

package org.rainfall.unit;

import org.rainfall.Unit;

import java.util.concurrent.TimeUnit;

/**
 * This bookkeeping class contains the instances of {@link org.rainfall.Unit} classes.
 * A Unit class defines the execution of an {@link org.rainfall.Operation} according to its parameters
 *
 * @author Aurelien Broszniowski
 */

public class Units {

  public static Unit users = new User();

  public static TimeDivision seconds = new TimeDivision(TimeUnit.SECONDS);

  public static TimeDivision minutes = new TimeDivision(TimeUnit.MINUTES);

}
