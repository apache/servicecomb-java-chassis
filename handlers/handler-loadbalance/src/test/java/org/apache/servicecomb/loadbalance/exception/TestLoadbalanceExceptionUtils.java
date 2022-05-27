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

package org.apache.servicecomb.loadbalance.exception;

import org.apache.servicecomb.core.exception.CseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLoadbalanceExceptionUtils {

  @Test
  public void testLoadbalanceExceptionUtils() {

    Assertions.assertEquals("servicecomb.handler.lb.wrong.rule", LoadbalanceExceptionUtils.CSE_HANDLER_LB_WRONG_RULE);
    CseException cseException = LoadbalanceExceptionUtils.createLoadbalanceException("servicecomb.handler.lb.wrong.rule",
        new Throwable(),
        "ARGS");
    Assertions.assertNotNull(cseException);
  }
}
