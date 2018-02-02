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

package io.rainfall.generator;

import io.rainfall.ObjectGenerator;

/**
 * @author Aurelien Broszniowski
 */

public class StringGenerator implements ObjectGenerator<String> {

  private final String padding;
  private int length;

  public StringGenerator(final int length) {
    this.length = length;
    if (length <= 0) {
      throw new IllegalStateException("Can not generate a String with a length less or equal to 0");
    }
    StringBuffer outputBuffer = new StringBuffer(length);
    for (int i = 0; i < length - 1; i++) {
      outputBuffer.append("0");
    }
    this.padding = outputBuffer.toString();
  }

  @Override
  public String generate(final Long seed) {
    String s = padding + seed;
    return s.substring(s.length() - length);
  }

  @Override
  public String getDescription() {
    return "String (length = " + length + ")";
  }

  public static ObjectGenerator<String> fixedLengthString(final int length) {
    return new StringGenerator(length);
  }
}
