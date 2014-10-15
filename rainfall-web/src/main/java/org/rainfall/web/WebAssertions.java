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

package org.rainfall.web;

import org.rainfall.Unit;
import org.rainfall.web.assertion.LessThanComparator;
import org.rainfall.web.assertion.ResponseTime;

/**
 * @author Aurelien Broszniowski
 */

public class WebAssertions {

  public static ResponseTime responseTime() {
    return new ResponseTime();
  }
//TODO : use matchers ? or other assertion api? to extend?

  public static LessThanComparator isLessThan(long value, Unit unit) {
    return new LessThanComparator(value, unit);
  }

}
