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

package io.servicecomb.serviceregistry.api.registry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;

public class TestGetFrameworkVersion {

  ClassLoader classLoader = null;

  @Before
  public void setUp() throws Exception {
    classLoader = Thread.currentThread().getContextClassLoader();
  }

  @After
  public void tearDown() throws Exception {
    classLoader = null;
  }

  @Test
  public void test() {
    @SuppressWarnings("deprecation")
	Version version = VersionUtil.mavenVersionFor(classLoader, "io.servicecomb", "java-chassis");

    Assert.assertEquals("1.0.0-test", version.toString());
  }

}
