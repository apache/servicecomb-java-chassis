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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.param.PathProcessorCreator.PathProcessor;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestPathProcessor {
  final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  final Map<String, String> pathVars = new HashMap<>();

  ParamValueProcessor processor;

  private void createProcessor(String name, Class<?> type) {
    processor = new PathProcessor(name, TypeFactory.defaultInstance().constructType(type), null, true);
  }

  private void prepareGetValue(String name, Class<?> type) {
    createProcessor(name, type);
    Mockito.when(request.getAttribute(RestConst.PATH_PARAMETERS)).thenReturn(pathVars);
  }

  @Test
  public void testGetValueNoPathVars() throws Exception {
    createProcessor("name", String.class);

    Assertions.assertNull(processor.getValue(request));
  }

  @Test
  public void testGetValuePathNotFound() throws Exception {
    prepareGetValue("name", String.class);

    Assertions.assertNull(processor.getValue(request));
  }

  @Test
  public void testGetValuePathNormal() throws Exception {
    prepareGetValue("name", String.class);
    pathVars.put("name", "value");

    Assertions.assertEquals("value", processor.getValue(request));
  }

  @Test
  public void testGetSpaceEncoded() throws Exception {
    prepareGetValue("name", String.class);
    pathVars.put("name", "a%20b");

    Assertions.assertEquals("a b", processor.getValue(request));
  }

  @Test
  public void testGetPlus() throws Exception {
    prepareGetValue("name", String.class);
    pathVars.put("name", "a+b");

    Assertions.assertEquals("a+b", processor.getValue(request));
  }

  @Test
  public void testGetPercentage() throws Exception {
    prepareGetValue("name", String.class);
    pathVars.put("name", "%25%25");

    Assertions.assertEquals("%%", processor.getValue(request));
  }

  @Test
  public void testGetColon() throws Exception {
    prepareGetValue("name", String.class);
    pathVars.put("name", "aa:bb");
    Assertions.assertEquals("aa:bb", processor.getValue(request));
  }

  @Test
  public void testGetProcessorType() {
    createProcessor("name", String.class);
    Assertions.assertEquals("path", processor.getProcessorType());
  }
}
