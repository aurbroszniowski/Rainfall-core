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

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * @author Aurelien Broszniowski
 */

public class ByteArrayGenerator implements ObjectGenerator<Byte[]> {

  private final int length;
  SecureRandom rnd = new SecureRandom();

  public ByteArrayGenerator(final int length) {
    this.length = length;
  }

  @Override
  public Byte[] generate(final long seed) {
    Byte[] randomBytes = new Byte[length];
    for (int i = 0, len = randomBytes.length; i < len; i++) {
      randomBytes[i] = (byte)i;
    }
    return randomBytes;
  }

  public static ObjectGenerator<Byte[]> fixedLength(final int length) {
    return new ByteArrayGenerator(length);
  }
}
