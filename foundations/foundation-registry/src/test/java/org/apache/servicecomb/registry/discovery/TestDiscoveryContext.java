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

package org.apache.servicecomb.registry.discovery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestDiscoveryContext {
  DiscoveryContext context = new DiscoveryContext();

  @Test
  public void inputParameters() {
    Object inputParameters = new Object();
    context.setInputParameters(inputParameters);

    Assertions.assertSame(inputParameters, context.getInputParameters());
  }

  @Test
  public void contextParameters() {
    String name = "name";
    Object value = new Object();
    context.putContextParameter(name, value);

    Assertions.assertSame(value, context.getContextParameter(name));
    Assertions.assertNull(context.getContextParameter("notExist"));
  }

  @Test
  public void rerun() {
    Assertions.assertNull(context.popRerunFilter());

    DiscoveryTreeNode node = new DiscoveryTreeNode();
    context.setCurrentNode(node);
    context.pushRerunFilter();

    Assertions.assertSame(node, context.popRerunFilter());
    Assertions.assertNull(context.popRerunFilter());
  }
}
