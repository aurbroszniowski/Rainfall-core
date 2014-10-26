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

package org.rainfall.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A special Map, where keys are double values defining ranges.
 * E.g.
 *    map.put(0.10, "value1");
 *    map.put(0.20, "value2");
 *    map.put(0.15, "value3");
 *
 * The value1 can be fetched for a value between 0.0 (inclusive) and 0.10 (exclusive)
 *
 * Next, the lower value of the range is the higher value of the previous key (0.10), and the higher range will be the
 *   lower range value + the key value (0.10 + 0.20)
 * the value2 can be fetched for a key between 0.10 (inclusive) and 0.30 (exclusive).
 *
 * Next, the lower value of the range is the higher value of the previous key (0.30), and the higher range will be the
 *   lower range value + the key value (0.30 + 0.15)
 * the value3 can be fetched for a key between 0.30 (inclusive) and 0.45 (exclusive).
 *
 * Fetching a value for a key higher than 0.45 will return null
 *
 * @author Aurelien Broszniowski
 */

public class RangeMap<E> {

  private final Map<Double, E> values = new HashMap<Double, E>();
  private final List<Range> keys = new LinkedList<Range>();
  private Double higherBound = 0.0;

  public synchronized E put(final Double key, final E value) {
    E put = values.put(higherBound, value);
    keys.add(new Range(higherBound, higherBound + key, higherBound));
    higherBound += key;
    return put;
  }

  public E get(final double key) {
    for (Range range : keys) {
      if (range.contains(key))
        return values.get(range.getKey());
    }
    return null;
  }

  public class Range {
    private double low;
    private double high;
    private Double key;

    public Range(final double low, final double high, final Double key) {
      this.low = low;
      this.high = high;
      this.key = key;
    }

    public boolean contains(final double key) {
      return (this.low <= key && this.high > key);
    }

    public Double getKey() {
      return key;
    }
  }
}
