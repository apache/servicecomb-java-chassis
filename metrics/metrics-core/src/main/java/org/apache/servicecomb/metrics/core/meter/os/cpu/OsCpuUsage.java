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

import java.io.IOException;

import com.netflix.spectator.api.Id;

/*
 * unit : 1 jiffies
 * more details :
 * http://man7.org/linux/man-pages/man5/proc.5.html
 * CMD :  /proc/stat
 * cpu  2445171 599297 353967 24490633 11242   0    10780    2993             0      0
 * cpu  user    nice   system idle     iowait  irq  softirq  stealstolen      guest  guest_nice
 * 0    1       2      3      4        5        6   7        8                9      10
 * total = user + nice + system + idle + iowait + irq + softirq + stealstolen
 * busy = total - idle
 */
public class OsCpuUsage extends AbstractCpuUsage {

  private Period total = new Period();

  private Period idle = new Period();

  public OsCpuUsage(Id id) {
    super(id);
  }

  public void update() throws IOException {
    String[] stats = CpuUtils.readAndSplitFirstLine(CpuUtils.PROC_STAT);
    long currentIdle = Long.parseLong(stats[4]);
    double totalCpu = CpuUtils.summary(stats, 1, 8);
    idle.update(currentIdle);
    total.update(totalCpu);
    updateUsage(total.period - idle.period, total.period, false);
  }
}
