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

package org.apache.servicecomb.swagger.invocation.context;

/**
 * 传递调用过程的上下文数据
 */
public final class ContextUtils {
  private ContextUtils() {
  }

  private static ThreadLocal<InvocationContext> contextMgr = new ThreadLocal<>();

  public static InvocationContext getInvocationContext() {
    return contextMgr.get();
  }

  public static InvocationContext getAndRemoveInvocationContext() {
    InvocationContext context = contextMgr.get();
    if (context != null) {
      contextMgr.remove();
    }
    return context;
  }

  public static void setInvocationContext(InvocationContext invocationContext) {
    contextMgr.set(invocationContext);
  }

  public static void removeInvocationContext() {
    contextMgr.remove();
  }
}
