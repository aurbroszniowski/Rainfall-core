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

package org.rainfall.generator;

import org.rainfall.ObjectGenerator;

import java.util.UUID;

/**
 * @author Aurelien Broszniowski
 */

public class StringGenerator implements ObjectGenerator {

  private final String randomString;

  public StringGenerator(final int length) {
    if (length <= 0) {
      throw new IllegalStateException("Can not generate a String with a length less or equal to 0");
    }
    String baseRandom = UUID.randomUUID().toString();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1 + (length / baseRandom.length()); i++)
      sb.append(baseRandom);
    this.randomString = sb.subSequence(0, length).toString();
  }

  @Override
  public String generate(final long seed) {
    return "" + this.randomString;   // return a new instance
  }

  public static ObjectGenerator fixedLength(final int length) {
    return new StringGenerator(length);
  }
}
