/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ThreadLocalMonitorManager {
  private static ThreadLocal<InvocationThreadLocalMonitor> localMonitor = new ThreadLocal<>();

  private static List<InvocationThreadLocalMonitor> allThreadMonitor = new CopyOnWriteArrayList<>();

  public static InvocationThreadLocalMonitor getInvocationMonitor() {
    InvocationThreadLocalMonitor monitor = localMonitor.get();
    if (monitor == null) {
      monitor = new InvocationThreadLocalMonitor();
      allThreadMonitor.add(monitor);
      localMonitor.set(monitor);
    }
    return monitor;
  }

  public static List<InvocationThreadLocalCache> getAllInvocationThreadLocalCache() {
    List<InvocationThreadLocalCache> caches = new ArrayList<>();
    for (InvocationThreadLocalMonitor monitor : allThreadMonitor) {
      caches.addAll(monitor.collectInvocationThreadLocalCache());
    }
    return caches;
  }
}
