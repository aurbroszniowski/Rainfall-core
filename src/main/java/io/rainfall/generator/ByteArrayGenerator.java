/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
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

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Aurelien Broszniowski
 */

public class ByteArrayGenerator implements ObjectGenerator<byte[]> {

  private final int length;

  public ByteArrayGenerator(final int length) {
    this.length = length;
  }

  @Override
  public byte[] generate(Long seed) {
    byte[] object = new byte[length];
    Arrays.fill(object, (byte)ThreadLocalRandom.current().nextInt());
    return object;
  }

  @Override
  public String getDescription() {
    return "byte[" + length + "]";
  }

  public static ObjectGenerator<byte[]> fixedLengthByteArray(final int length) {
    return new ByteArrayGenerator(length);
  }
}
