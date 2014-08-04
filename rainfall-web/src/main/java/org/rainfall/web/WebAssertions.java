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
