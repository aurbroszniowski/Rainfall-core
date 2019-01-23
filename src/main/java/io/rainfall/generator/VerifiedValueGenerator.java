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

/**
 * @author Aurelien Broszniowski
 */
public class VerifiedValueGenerator<K> implements ObjectGenerator<VerifiedValueGenerator.VerifiedValue> {

  private ObjectGenerator<K> keyGenerator;

  public VerifiedValueGenerator(ObjectGenerator<K> keyGenerator) {
    this.keyGenerator = keyGenerator;
  }

  @Override
  public VerifiedValue generate(Long seed) {
    return new VerifiedValue<K>(seed, keyGenerator.generate(seed));
  }

  @Override
  public String getDescription() {
    return "VerifiedValue (custom object, with equality assertion)";
  }

  public static class VerifiedValue<K> {
    private final K k;
    private Long key;

    public VerifiedValue(Long key, K k) {
      this.key = key;
      this.k = k;
    }

    public Long getKey() {
      return key;
    }

    @Override
    public boolean equals(final Object obj) {
      if (!(obj instanceof VerifiedValue)) {
        return false;
      }
      return ((VerifiedValue)obj).getKey().equals(getKey());
    }
  }
}
