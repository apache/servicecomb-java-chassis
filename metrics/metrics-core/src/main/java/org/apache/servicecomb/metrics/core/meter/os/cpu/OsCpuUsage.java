/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.metrics.core.meter.os.cpu;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.spectator.api.Id;

/*
 * unit : 1 jiffies = 10ms = 0.01 s
 * more details :
 * http://man7.org/linux/man-pages/man5/proc.5.html
 * CMD :  /proc/stat
 * cpu  2445171 599297 353967 24490633 11242   0    10780    2993             0      0
 * cpu  user    nice   system idle     iowait  irq  softirq  stealstolen      guest  guest_nice
 * 0    1       2      3      4        5        6   7        8
 * total = user + nice + system + idle + iowait + irq + softirq + stealstolen
 * busy = total - idle
 */
public class OsCpuUsage extends AbstractCpuUsage {
  private long lastTotalTime;

  private long currentTotalTime;

  public OsCpuUsage(Id id) {
    super(id, "/proc/stat");
  }

  @Override
  protected void update(String[] stats) {
    currentTotalTime = readCurrentTotalTime(stats);
    periodTotalTime = currentTotalTime - lastTotalTime;
    lastTotalTime = currentTotalTime;

    super.update(stats);
  }

  private long readCurrentTotalTime(String[] stats) {
    long total = 0L;
    for (int i = 1; i <= 8; i++) {
      total += Long.parseLong(stats[i]);
    }
    return total;
  }

  @Override
  protected long readCurrentBusyTime(String[] stats) {
    return currentTotalTime - Long.parseLong(stats[4]);
  }

  @VisibleForTesting
  public long getLastTotalTime() {
    return lastTotalTime;
  }
}
