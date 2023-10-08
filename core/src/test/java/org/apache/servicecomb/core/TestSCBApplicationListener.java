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

import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;

import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;

public class TestSCBApplicationListener {
  @BeforeEach
  public void before() {
  }

  @AfterAll
  public static void classTeardown() {
  }

  @Test
  public void onApplicationEvent_close() {
    ContextClosedEvent contextClosedEvent = Mockito.mock(ContextClosedEvent.class);
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest();
    Environment environment = Mockito.mock(Environment.class);
    scbEngine.setEnvironment(environment);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);
    scbEngine.setStatus(SCBStatus.UP);

    SCBApplicationListener listener = new SCBApplicationListener(scbEngine);
    listener.onApplicationEvent(contextClosedEvent);

    Assertions.assertEquals(SCBStatus.DOWN, scbEngine.getStatus());

    scbEngine.destroy();
  }
}
