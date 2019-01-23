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

package io.rainfall.utils;

import io.rainfall.ObjectGenerator;

/**
 * @author Aurelien Broszniowski
 */

public class NullObjectGenerator<T> implements ObjectGenerator<T> {

  @Override
  public T generate(final long seed) {
    throw new IllegalStateException("You must define an ObjectGenerator.");
  }

  @Override
  public String getDescription() {
    return "Undefined";
  }

  public static <T> ObjectGenerator<T> instance() {
    return new NullObjectGenerator<T>();
  }
}
