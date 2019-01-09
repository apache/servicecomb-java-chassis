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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public final class CpuUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CpuUtils.class);

  public static final File PROC_STAT = new File("/proc/stat");

  public static final File UPTIME = new File("/proc/uptime");

  public static final File SELF_PROCESS = new File("/proc/self/stat");

  private CpuUtils() {
  }

  public static String[] readAndSplitFirstLine(String filePath) {
    return readAndSplitFirstLine(new File(filePath));
  }

  public static String[] readAndSplitFirstLine(File file) {
    try {
      return Files.asCharSource(file, StandardCharsets.UTF_8).readFirstLine().trim().split("\\s+");
    } catch (IOException | NullPointerException e) {
      LOGGER.error(String.format("Failed to read file %s", file.getName()), e);
    }
    return null;
  }

  public static double summary(String[] stats, int start, int len) {
    double total = 0;
    for (int idx = start; idx < start + len; idx++) {
      total += Double.parseDouble(stats[idx]);
    }
    return total;
  }

  private static double readProcStatTotal() {
    String[] stats = readAndSplitFirstLine(PROC_STAT);
    return summary(stats, 1, 8);
  }

  public static double readProcStatTotal(String[] stats) {
    return summary(stats, 1, 8);
  }

  private static double readUptimeTotal() {
    String[] uptime = readAndSplitFirstLine(UPTIME);
    return Double.parseDouble(uptime[0]);
  }

  private static boolean isBetween(long x, long lower, long upper) {
    return lower <= x && x <= upper;
  }

  /**
   *  unit of /proc/uptime is seconds
   *  unit of /proc/self/stat is jiffies
   *  hence, we should calculate userHZ to get process cpu rate
   *
   * @return userHZ
   */
  public static int calcHertz() {
    double up1, up2, seconds;
    double jiffies;

    for (; ; ) {
      try {
        up1 = readUptimeTotal();
        jiffies = readProcStatTotal();
        up2 = readUptimeTotal();
      } catch (Throwable e) {
        LOGGER.error("Failed to calc hertz, should never happened, try again.", e);
        continue;
      }

      /* want under 0.1% error */
      if (0 == (long) ((up2 - up1) * 1000.0 / up1)) {
        break;
      }
    }

    seconds = (up1 + up2) / 2;
    long hz = Math.round(jiffies / seconds / Runtime.getRuntime().availableProcessors());
    /* actual values used by 2.4 kernels: 32 64 100 128 1000 1024 1200 */
    /* S/390 (sometimes) */
    if (isBetween(hz, 9, 11)) {
      return 10;
    }

    /* user-mode Linux */
    if (isBetween(hz, 18, 22)) {
      return 20;
    }

    /* ia64 emulator */
    if (isBetween(hz, 30, 34)) {
      return 32;
    }

    if (isBetween(hz, 48, 52)) {
      return 50;
    }

    if (isBetween(hz, 58, 61)) {
      return 60;
    }

    /* StrongARM /Shark */
    if (isBetween(hz, 62, 65)) {
      return 64;
    }

    /* normal Linux */
    if (isBetween(hz, 95, 105)) {
      return 100;
    }

    /* MIPS, ARM */
    if (isBetween(hz, 124, 132)) {
      return 128;
    }

    /* normal << 1 */
    if (isBetween(hz, 195, 204)) {
      return 200;
    }

    if (isBetween(hz, 247, 252)) {
      return 250;
    }

    if (isBetween(hz, 253, 260)) {
      return 256;
    }

    /* normal << 2 */
    if (isBetween(hz, 393, 408)) {
      return 400;
    }

    /* SMP WinNT */
    if (isBetween(hz, 410, 600)) {
      return 500;
    }

    /* normal << 3 */
    if (isBetween(hz, 790, 808)) {
      return 800;
    }

    /* ARM */
    if (isBetween(hz, 990, 1010)) {
      return 1000;
    }

    /* Alpha, ia64 */
    if (isBetween(hz, 1015, 1035)) {
      return 1024;
    }

    /* Alpha */
    if (isBetween(hz, 1180, 1220)) {
      return 1200;
    }

    LOGGER.warn("Unknown HZ value! ({}) Assume {}.\n", hz, 100);
    return 100;
  }
}
