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

package org.apache.servicecomb.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.executor.FixedThreadExecutor;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.junit.Assert;
import org.junit.Test;

import mockit.Deencapsulation;

public class TestExecutors {

  String strThreadTest = "default";

  @Test
  public void testFixedThreadExecutor() {
    FixedThreadExecutor oFixedThreadExecutor = new FixedThreadExecutor();
    oFixedThreadExecutor.execute(new Runnable() {

      @Override
      public void run() {

      }
    });
    Map<Long, Executor> threadExecutorMap = Deencapsulation.getField(oFixedThreadExecutor, "threadExecutorMap");
    Assert.assertEquals(true, (threadExecutorMap.size() > 0));

    List<Executor> executorList = Deencapsulation.getField(oFixedThreadExecutor, "executorList");
    Assert.assertEquals(true, (executorList.size() > 1));

    ReactiveExecutor oReactiveExecutor = new ReactiveExecutor();
    oReactiveExecutor.execute(new Runnable() {

      @Override
      public void run() {
        strThreadTest = "thread Ran";
      }
    });
    Assert.assertEquals("thread Ran", strThreadTest);
  }
}
