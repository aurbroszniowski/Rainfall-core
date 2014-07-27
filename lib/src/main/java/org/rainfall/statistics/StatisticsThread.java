package org.rainfall.statistics;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsThread extends Thread {

  boolean stopped = false;

  @Override
  public void run() {
    while (!stopped) {
      ConcurrentHashMap<String, StatisticsObserver> statisticObservers = StatisticsManager.getStatisticObservers();
      for (StatisticsObserver observer : statisticObservers.values()) {
        System.out.println(observer.toString());
      }
      System.out.println("----------------------------------------------");
      try {
        sleep(1000);
      } catch (InterruptedException e) {
        this.stopped = true;
      }
    }
  }

  public void end() {
    this.stopped = true;
  }
}
