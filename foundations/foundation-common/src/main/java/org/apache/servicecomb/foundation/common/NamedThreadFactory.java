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

package org.apache.servicecomb.foundation.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 已命名线程工厂.
 * 获取新的名字有固定前缀的线程工厂.
 */
public class NamedThreadFactory implements ThreadFactory {

  private final AtomicInteger threadNumber = new AtomicInteger();

  private String prefix;

  public NamedThreadFactory() {
    this("Thread");
  }

  public NamedThreadFactory(String prefix) {
    this.prefix = prefix;
  }

  /**
   * 获取新的名字以prefix为前缀的线程
   */
  @Override
  public Thread newThread(Runnable r) {
    return new Thread(r, prefix + "-" + threadNumber.getAndIncrement());
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}
