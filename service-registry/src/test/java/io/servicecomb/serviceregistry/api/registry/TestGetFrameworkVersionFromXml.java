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

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;

public class TestGetFrameworkVersionFromXml {

  GetFrameworkVersionFromXml getFrameworkVersionFromXml = null;

  @Before
  public void setUp() throws Exception {
    getFrameworkVersionFromXml = new GetFrameworkVersionFromXml();
  }

  @After
  public void tearDown() throws Exception {
    getFrameworkVersionFromXml = null;
  }

  @Test
  public void test() throws FileNotFoundException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream is = loader.getResourceAsStream("/config/frameworkVersion.properties");
    new Expectations() {
      {
        GetFrameworkVersionFromXml.class.getResourceAsStream(anyString);
        result = is;
      }
    };
    Assert.assertEquals("0.6.0", getFrameworkVersionFromXml.getFrameworkVersion());
  }

}
