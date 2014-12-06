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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A special Map, where keys are float values defining ranges.
 * E.g.
 * map.put(0.10, "value1");
 * map.put(0.20, "value2");
 * map.put(0.15, "value3");
 * <p/>
 * The value1 can be fetched for a value between 0.0 (inclusive) and 0.10 (exclusive)
 * <p/>
 * Next, the lower value of the range is the higher value of the previous key (0.10), and the higher range will be the
 * lower range value + the key value (0.10 + 0.20)
 * the value2 can be fetched for a key between 0.10 (inclusive) and 0.30 (exclusive).
 * <p/>
 * Next, the lower value of the range is the higher value of the previous key (0.30), and the higher range will be the
 * lower range value + the key value (0.30 + 0.15)
 * the value3 can be fetched for a key between 0.30 (inclusive) and 0.45 (exclusive).
 * <p/>
 * Fetching a value for a key higher than 0.45 will return null
 *
 * @author Aurelien Broszniowski
 */

public class RangeMap<E> {

  private final Map<Float, E> values = new HashMap<Float, E>();
  private final List<Range> keys = new LinkedList<Range>();
  private Float higherBound = 0.0f;
  private final ConcurrentPseudoRandom rnd = new ConcurrentPseudoRandom();

  public synchronized E put(final Float key, final E value) {
    E put = values.put(higherBound, value);
    keys.add(new Range(higherBound, higherBound + key, higherBound));
    higherBound += key;
    return put;
  }

  public E get(final float key) {
    for (Range range : keys) {
      if (range.contains(key))
        return values.get(range.getKey());
    }
    return null;
  }

  public Float getHigherBound() {
    return higherBound;
  }

  public Collection<E> getAll() {
    return values.values();
  }

  public class Range {
    private float low;
    private float high;
    private Float key;

    public Range(final float low, final float high, final Float key) {
      this.low = low;
      this.high = high;
      this.key = key;
    }

    public boolean contains(final float key) {
      return (this.low <= key && this.high > key);
    }

    public Float getKey() {
      return key;
    }
  }
}
