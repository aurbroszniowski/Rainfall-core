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

import org.junit.Test;
import io.rainfall.ObjectGenerator;

import org.junit.Assert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Aurelien Broszniowski
 */

public class StringGeneratorTest {

  @Test
  public void testGenerateInvalidFixedLengthString() {
    try {
      ObjectGenerator generator = StringGenerator.fixedLength(-1);
      generator.generate(0L);
      Assert.fail("Should not be able to generate a negative length String");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testGenerateZeroLengthString() {
    try {
      ObjectGenerator generator = StringGenerator.fixedLength(0);
      generator.generate(0L);
      Assert.fail("Should not be able to generate a 0 length String");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testGenerate1CharacterString() {
    int length = 1;
    ObjectGenerator generator = StringGenerator.fixedLength(length);
    String generated = (String)generator.generate(0L);
    assertThat(generated.length(), is(equalTo(length)));
  }

  @Test
  public void testGenerate10CharactersString() {
    int length = 10;
    ObjectGenerator generator = StringGenerator.fixedLength(length);
    String generated = (String)generator.generate(0L);
    assertThat(generated.length(), is(equalTo(length)));
  }

  @Test
  public void testGenerate100CharactersString() {
    int length = 100;
    ObjectGenerator generator = StringGenerator.fixedLength(length);
    String generated = (String)generator.generate(0L);
    assertThat(generated.length(), is(equalTo(length)));
  }

 @Test
  public void testTwoGenerationsGiveDifferentStringInstances() {
    int length = 10;
    ObjectGenerator generator = StringGenerator.fixedLength(length);
    String generated1 = (String)generator.generate(0L);
    String generated2 = (String)generator.generate(0L);
   assertThat(generated1, not(sameInstance(generated2)));
  }


}
