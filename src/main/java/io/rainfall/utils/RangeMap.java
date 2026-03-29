/*
 * Copyright (c) 2014-2020 Aurélien Broszniowski
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A special Map, where keys are float values defining ranges.
 * E.g.
 * map.put(0.10, "value1");
 * map.put(0.20, "value2");
 * map.put(0.15, "value3");
 *
 * The value1 can be fetched for a value between 0.0 (inclusive) and 0.10 (exclusive)
 *
 * Next, the lower value of the range is the higher value of the previous key (0.10), and the higher range will be the
 * lower range value + the key value (0.10 + 0.20)
 * the value2 can be fetched for a key between 0.10 (inclusive) and 0.30 (exclusive).
 *
 * Next, the lower value of the range is the higher value of the previous key (0.30), and the higher range will be the
 * lower range value + the key value (0.30 + 0.15)
 * the value3 can be fetched for a key between 0.30 (inclusive) and 0.45 (exclusive).
 *
 * Fetching a value for a key higher than 0.45 will return null
 *
 * @author Aurelien Broszniowski
 */

public class RangeMap<E> {

  private static final int DEFAULT_CAPACITY = 4;

  private float[] upperBounds = new float[DEFAULT_CAPACITY];
  private Object[] values = new Object[DEFAULT_CAPACITY];
  private int size = 0;
  private float higherBound = 0.0f;

  public synchronized void put(final Float weight, final E value) {
    if (weight > 0) {
      ensureCapacity(size + 1);
      higherBound += weight;
      upperBounds[size] = higherBound;
      values[size] = value;
      size++;
    }
  }

  public E get(final float key) {
    if (key < 0.0f || key >= higherBound || size == 0) {
      return null;
    }

    for (int i = 0; i < size; i++) {
      if (key < upperBounds[i]) {
        return valueAt(i);
      }
    }
    return null;
  }

  public Float getHigherBound() {
    return higherBound;
  }

  public Collection<E> getAll() {
    List<E> all = new ArrayList<E>(size);
    for (int i = 0; i < size; i++) {
      all.add(valueAt(i));
    }
    return all;
  }

  public E getNextRandom(ConcurrentPseudoRandom concurrentPseudoRandom) {
    return get(concurrentPseudoRandom.nextFloat(higherBound));
  }

  private void ensureCapacity(int capacity) {
    if (capacity <= upperBounds.length) {
      return;
    }
    int newCapacity = Math.max(capacity, upperBounds.length * 2);
    float[] expandedUpperBounds = new float[newCapacity];
    Object[] expandedValues = new Object[newCapacity];
    System.arraycopy(upperBounds, 0, expandedUpperBounds, 0, size);
    System.arraycopy(values, 0, expandedValues, 0, size);
    upperBounds = expandedUpperBounds;
    values = expandedValues;
  }

  @SuppressWarnings("unchecked")
  private E valueAt(int index) {
    return (E) values[index];
  }
}
