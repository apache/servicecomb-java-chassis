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

package org.apache.servicecomb.spring.cloud.zuul.tracing;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_ENABLED_KEY;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class SpringTracingConfigurationTest {

  private AnnotationConfigApplicationContext context;

  @Before
  public void setUp() {
    context = new AnnotationConfigApplicationContext();
  }

  @After
  public void tearDown() {
    context.close();
    System.clearProperty(CONFIG_TRACING_ENABLED_KEY);
  }

  @Test
  public void ensureTracingDisabled() {
    System.setProperty(CONFIG_TRACING_ENABLED_KEY, "false");
    context.register(SpringTracingConfiguration.class);
    context.refresh();
    try {
      context.getBean(SpringTracingConfiguration.class);
      fail("Expect to throw BeansException, but got none");
    } catch (BeansException ignored) {
    }
  }

  @Test
  public void ensureTracingEnabledByDefault() {
    tracingEnabledCheck();
  }

  @Test
  public void ensureTracingEnabledByProperty() {
    System.setProperty(CONFIG_TRACING_ENABLED_KEY, "true");
    tracingEnabledCheck();
  }

  private void tracingEnabledCheck() {
    context.register(SpringTracingConfiguration.class);
    try {
      context.refresh();
      fail("Expect to throw UnsatisfiedDependencyException, but got none");
    } catch (UnsatisfiedDependencyException ignored) {
    } catch (Exception e) {
      fail("Expect to throw UnsatisfiedDependencyException, but got Exception: " + e);
    }
  }
}
