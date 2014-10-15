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

package org.rainfall.statistics;

import jsr166e.ConcurrentHashMapV8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Aurelien Broszniowski
 */

public class MeldablePriorityQueue<P extends Comparable<P>, E> {

  private final Comparator<E> comparator;
  private Set<E> set = new HashSet<E>();
  private final ConcurrentHashMapV8<P, E> statisticsMap = new ConcurrentHashMapV8<P, E>();

  public MeldablePriorityQueue(final Comparator<E> comparator) {
    this.comparator = comparator;
    this.set = new HashSet<E>();
  }

  public E meld(P priority, E value, ConcurrentHashMapV8.BiFun<? super E, ? super E, ? extends E> remappingFunction) {
    return this.statisticsMap.merge(priority, value, remappingFunction);
  }

  public List<E> peekAll(P priority) {
    List<E> results = new ArrayList<E>();
    synchronized (this.statisticsMap) {
      results.addAll(this.statisticsMap.values());
      this.statisticsMap.clear();
    }
    Collections.sort(results, comparator);
    return results;
  }

  public int size() {
    return statisticsMap.size();
  }
}
