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
package org.apache.servicecomb.samples.apm.impl;

public class ApmContextUtils {
  private static ThreadLocal<ApmContext> threadApmContext = new ThreadLocal<>();

  public static ApmContext getApmContext() {
    return threadApmContext.get();
  }

  public static ApmContext getAndRemoveApmContext() {
    ApmContext context = threadApmContext.get();
    if (context != null) {
      threadApmContext.remove();
    }
    return context;
  }

  public static void setApmContext(ApmContext apmContext) {
    threadApmContext.set(apmContext);
  }

  public static void removeApmContext() {
    threadApmContext.remove();
  }
}
