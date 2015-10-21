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
package io.rainfall.reporting;


import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Ludovic Orban
 */
public class GcStatsCollector {

  private static final String GARBAGE_COLLECTION_NOTIFICATION = "com.sun.management.gc.notification";

  public static class GcStats {
    public enum Header {
      DURATION,ACTION,CAUSE,NAME
    }

    private final long startTimestamp;
    private final long duration;
    private final String action;
    private final String cause;
    private final String name;

    public GcStats(long duration, String action, long startTimestamp, String cause, String name) {
      this.duration = duration;
      this.action = action;
      this.startTimestamp = startTimestamp;
      this.cause = cause;
      this.name = name;
    }

    public long getStartTimestamp() {
      return startTimestamp;
    }

    public long getDuration() {
      return duration;
    }

    public String getAction() {
      return action;
    }

    public String getCause() {
      return cause;
    }

    public String getName() {
      return name;
    }
  }

  private final Queue<GcStats> gcStatsQueue = new ConcurrentLinkedQueue<GcStats>();

  private final NotificationListener listener = new NotificationListener() {
    @Override
    public void handleNotification(Notification notification, Object handback) {
      if (notification.getType().equals(GARBAGE_COLLECTION_NOTIFICATION)) {
        CompositeData userData = (CompositeData)notification.getUserData();
        CompositeData gcInfo = (CompositeData) userData.get("gcInfo");

        GcStats gcStats = new GcStats(
            (Long) gcInfo.get("duration"),
            (String) userData.get("gcAction"),
            (Long) gcInfo.get("startTime"),
            (String) userData.get("gcCause"),
            (String) userData.get("gcName")
        );
        gcStatsQueue.add(gcStats);
      }
    }
  };

  public List<GcStats> drain() {
    List<GcStats> result = new ArrayList<GcStats>();
    while (true) {
      GcStats gcStats = gcStatsQueue.poll();
      if (gcStats == null) break;
      result.add(gcStats);
    }
    return result;
  }

  public void registerGcEventListeners() {
    List<GarbageCollectorMXBean> gcMxBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
      NotificationEmitter emitter = (NotificationEmitter) gcMxBean;

      emitter.addNotificationListener(listener, null, null);
    }
  }

  public void unregisterGcEventListeners() {
    List<GarbageCollectorMXBean> gcMxBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
      NotificationEmitter emitter = (NotificationEmitter) gcMxBean;

      try {
        emitter.removeNotificationListener(listener);
      } catch (ListenerNotFoundException e) {
        //
      }
    }
  }

}
