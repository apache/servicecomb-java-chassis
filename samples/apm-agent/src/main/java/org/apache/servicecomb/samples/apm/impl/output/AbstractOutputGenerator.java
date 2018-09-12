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
package org.apache.servicecomb.samples.apm.impl.output;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.event.InvocationFinishEvent;

import com.google.common.base.Strings;

public abstract class AbstractOutputGenerator {
  static final String PAD2_KEY11_FMT = generateFmt(2, 11);

  static final String PAD2_TIME_FMT = generateTimeFmt(2);

  static final String PAD4_TIME_FMT = generateTimeFmt(4);

  static final String PAD6_TIME_FMT = generateTimeFmt(6);

  protected static String generateFmt(int leftPad, int keyLen) {
    return Strings.repeat(" ", leftPad) + "%-" + keyLen + "s: %s\n";
  }

  protected static String generateTimeFmt(int leftPad) {
    return Strings.repeat(" ", leftPad) + "%-" + (27 - leftPad) + "s: %.3fms\n";
  }

  protected void appendLine(StringBuilder sb, String fmt, String headerKey, Object value) {
    sb.append(String.format(fmt, headerKey, value));
  }

  protected void appendTimeLine(StringBuilder sb, String fmt, String headerKey, double nano) {
    sb.append(String.format(fmt, headerKey, nano / TimeUnit.MILLISECONDS.toNanos(1)));
  }

  abstract public void generate(StringBuilder sb, InvocationFinishEvent event);
}
