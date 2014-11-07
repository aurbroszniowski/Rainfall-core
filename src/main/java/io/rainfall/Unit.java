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

import io.rainfall.unit.User;

/**
 * This is a Unit for the parameter given to a {@link Execution} instance.
 * E.g. an execution like {@link io.rainfall.execution.AtOnce} will have as parameters a number of
 * {@link Unit}
 *
 * @author Aurelien Broszniowski
 */

public abstract class Unit {

  public static final Unit users = new User();

}
