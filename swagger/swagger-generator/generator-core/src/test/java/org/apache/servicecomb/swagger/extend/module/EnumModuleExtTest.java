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

package org.apache.servicecomb.swagger.extend.module;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.swagger.extend.introspector.JsonPropertyIntrospectorTest.TestEnum;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.swagger.util.Json;

public class EnumModuleExtTest {
  @Test
  public void testEnumModule() throws JsonProcessingException {
    ObjectMapper mapper = Json.mapper();
    mapper.registerModule(new SimpleModule());

    String serializeValue = mapper.writeValueAsString(TestEnum.AB);
    assertEquals("\"AB\"", serializeValue);
    serializeValue = mapper.writeValueAsString(TestEnum.C_D);
    assertEquals("\"C-D\"", serializeValue);
    serializeValue = mapper.writeValueAsString(TestEnum.E_F);
    assertEquals("\"E.F\"", serializeValue);
    serializeValue = mapper.writeValueAsString(TestEnum.HI);
    assertEquals("\"HI\"", serializeValue);
  }
}
