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
package org.apache.servicecomb.config.priority;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.netflix.config.DynamicPropertyFactory;

public class TestPriorityPropertyBase {
  protected PriorityPropertyManager priorityPropertyManager;

  protected PriorityPropertyFactory propertyFactory;

  @BeforeEach
  public void setup() {
    // avoid write too many logs
    Logger.getRootLogger().setLevel(Level.OFF);

    ArchaiusUtils.resetConfig();

    // make sure create a DynamicPropertyFactory instance
    // otherwise will cause wrong order of configurationListeners
    DynamicPropertyFactory.getInstance();

    propertyFactory = new PriorityPropertyFactory();
    priorityPropertyManager = new PriorityPropertyManager(new ConfigObjectFactory(propertyFactory));

    Logger.getRootLogger().setLevel(Level.INFO);
  }

  @AfterEach
  public void teardown() {
    ArchaiusUtils.resetConfig();
  }
}
