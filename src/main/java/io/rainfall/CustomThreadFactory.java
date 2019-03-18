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

package io.rainfall;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Aurelien Broszniowski
 */

public class CustomThreadFactory implements ThreadFactory {
  private AtomicInteger cnt = new AtomicInteger();

  @Override
  public Thread newThread(Runnable runnable) {
    Thread t = new Thread(runnable, "Reporter-Thread_" + cnt.incrementAndGet());
    return t;
  }

}