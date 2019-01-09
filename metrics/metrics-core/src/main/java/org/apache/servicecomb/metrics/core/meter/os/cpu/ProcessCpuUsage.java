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

import com.netflix.spectator.api.Id;

/*
 * unit : 1 jiffies
 * more details :
 * http://man7.org/linux/man-pages/man5/proc.5.html
 * CMD :  /proc/[pid]/stat
 * 6754 (kubelet) S      1     995   995      0       -1     4202752   193281  592501546 0       12       1152076 907044 87991  113319  ..
 * pid  comm      state  ppid  pgrp  session  tty_nr  tpgid  flags     minflt  cminflt   majflt  cmajflt  utime   stime  cutime cstime
 * 0    1         2      3     4     5        6       7      8         9       10        11      12       13      14     15     16
 * busy = utime + stime
 *
 */
public class ProcessCpuUsage extends AbstractCpuUsage {
  private Period busy = new Period();

  private Period total = new Period();

  private int userHZ = CpuUtils.calcHertz();

  public ProcessCpuUsage(Id id) {
    super(id);
  }

  public void update() {
    String[] stats = CpuUtils.readAndSplitFirstLine(CpuUtils.SELF_PROCESS);
    String[] uptime = CpuUtils.readAndSplitFirstLine(CpuUtils.UPTIME);

    if (stats == null || stats.length < 15 || uptime == null) {
      return;
    }
    busy.update(Double.parseDouble(stats[13]) + Double.parseDouble(stats[14]));
    total.update(Double.parseDouble(uptime[0]) * userHZ * cpuCount);

    updateUsage(busy.period, total.period);
  }

  @Override
  protected void updateUsage(double periodBusy, double periodTotal) {
    usage = periodTotal == 0 ? 0 : periodBusy / periodTotal;
    if (usage > 1) {
      usage = 1;
    }
  }
}
