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

import java.util.TimerTask;

/**
 * A timer that fires at the top of every second.
 */
public class TopOfSecondTimer {

  private volatile Thread thread;
  private volatile TimerTask timerTask;

  public TopOfSecondTimer() {
  }

  public void cancel() {
    thread.interrupt();
    timerTask = null;
  }

  private void onTimer() {
    TimerTask copy = this.timerTask;
    if (copy != null) {
      copy.run();
    }
  }

  private static long tsOfNextInterval(long baseTs, long millis) {
    return (long) (millis * (Math.floor((double) baseTs / millis))) + millis;
  }

  public void scheduleAtFixedRate(TimerTask timerTask, final long intervalMillis) {
    if (intervalMillis % 1000 != 0) {
      throw new IllegalArgumentException("Scheduled interval must be a multiple of 1000 ms");
    }
    if (this.timerTask != null) {
      throw new IllegalStateException("Only one task can be scheduled per timer");
    }
    this.timerTask = timerTask;
    this.thread = new Thread("Rainfall-TopOfSecondTimer-thread") {
      @Override
      public void run() {
        while (true) {
          long now = System.currentTimeMillis();
          long nextSecond = tsOfNextInterval(now, intervalMillis);

          long sleepDelay = Math.max(0, nextSecond - now);
          if (sleepDelay > 0) {
            try {
              Thread.sleep(sleepDelay);
            } catch (InterruptedException e) {
              return;
            }
          }

          onTimer();
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
  }
}
