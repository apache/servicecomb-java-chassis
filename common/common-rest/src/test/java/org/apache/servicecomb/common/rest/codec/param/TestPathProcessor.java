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

package org.apache.servicecomb.common.rest.codec.param;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.param.PathProcessorCreator.PathProcessor;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

import mockit.Expectations;
import mockit.Mocked;

public class TestPathProcessor {
  @Mocked
  HttpServletRequest request;

  Map<String, String> pathVars = new HashMap<>();

  ParamValueProcessor processor;

  private void createProcessor(String name, Class<?> type) {
    processor = new PathProcessor(name, TypeFactory.defaultInstance().constructType(type));
  }

  private void prepareGetValue(String name, Class<?> type) {
    createProcessor(name, type);
    new Expectations() {
      {
        request.getAttribute(RestConst.PATH_PARAMETERS);
        result = pathVars;
      }
    };
  }

  @Test
  public void testGetValueNoPathVars() throws Exception {
    createProcessor("name", String.class);

    Assert.assertEquals(null, processor.getValue(request));
  }

  @Test
  public void testGetValuePathNotFound() throws Exception {
    prepareGetValue("name", String.class);

    Assert.assertEquals(null, processor.getValue(request));
  }

  @Test
  public void testGetValuePathNormal() throws Exception {
    prepareGetValue("name", String.class);
    pathVars.put("name", "value");

    Assert.assertEquals("value", processor.getValue(request));
  }

  @Test
  public void testGetValuePathEncoded() throws Exception {
    prepareGetValue("name", String.class);
    pathVars.put("name", URLEncoder.encode("a b", StandardCharsets.UTF_8.name()));

    Assert.assertEquals("a b", processor.getValue(request));
  }

  @Test
  public void testGetProcessorType() {
    createProcessor("name", String.class);
    Assert.assertEquals("path", processor.getProcessorType());
  }
}
