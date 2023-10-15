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

package org.apache.servicecomb.common.rest.definition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.path.PathRegExp;
import org.apache.servicecomb.common.rest.definition.path.QueryVarParamWriter;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class TestPath {
  static Environment environment = Mockito.mock(Environment.class);

  @BeforeAll
  public static void beforeClass() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.emptyAsNull", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.ignoreDefaultValue", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.ignoreRequiredCheck", boolean.class, false))
        .thenReturn(false);
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testPathRegExp() throws Exception {
    PathRegExp oPathRegExp = new PathRegExp("//{test}//");
    Assertions.assertEquals(1, oPathRegExp.getGroupCount());
    Assertions.assertEquals(0, oPathRegExp.getGroupWithRegExpCount());
    PathRegExp oSecondPathRegExp = new PathRegExp("{[^/:]+?}");
    Assertions.assertEquals(1, oSecondPathRegExp.getGroupCount());
    Assertions.assertEquals(1, oSecondPathRegExp.getGroupWithRegExpCount());
    Assertions.assertEquals("test/", PathRegExp.ensureEndWithSlash("test/"));
    Assertions.assertEquals("test/", PathRegExp.ensureEndWithSlash("test"));
    Assertions.assertNull(oSecondPathRegExp.match("{test/test}", null));
    Assertions.assertEquals("(]+?)/(.*)", (oSecondPathRegExp.toString()));
    Assertions.assertFalse(oSecondPathRegExp.isStaticPath());
    Assertions.assertEquals(0, oSecondPathRegExp.getStaticCharCount());
    Assertions.assertNotEquals(null, (oPathRegExp.match("//{test}//", new HashMap<>())));
    // Error Scenarios
    new PathRegExp("//{test \t}//");
    // Error Scenarios for double {{
    try {
      new PathRegExp("//{test{");
      Assertions.fail("an exception is expected!");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("A variable must not contain an extra"));
    }
    // Error Scenarios for illegal }}
    try {
      new PathRegExp("//}");
      Assertions.fail("an exception is expected!");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("is only allowed as"));
    }
    // Error Scenarios for illegal ;
    try {
      new PathRegExp("//;");
      Assertions.fail("an exception is expected!");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("matrix parameters are not allowed in"));
    }

    // Error Scenarios for NO } ;
    try {
      new PathRegExp("//{test");
      Assertions.fail("an exception is expected!");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("No '}' found after"));
    }
  }

  @Test
  public void testUrlPathBuilder() throws Exception {
    Map<String, RestParam> paramMap = new HashMap<>();

    Parameter pathParameter = new PathParameter();
    pathParameter.setName("id");
    pathParameter.setSchema(new Schema<>());
    RestParam oRestParam = new RestParam(null, pathParameter, int.class);
    paramMap.put(oRestParam.getParamName(), oRestParam);

    Parameter queryParameter = new QueryParameter();
    queryParameter.setName("q");
    queryParameter.setSchema(new Schema<>());
    oRestParam = new RestParam(null, queryParameter, String.class);
    paramMap.put(oRestParam.getParamName(), oRestParam);

    URLPathBuilder oURLPathBuilder = new URLPathBuilder("/root/{id}", paramMap);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("id", 100);
    parameters.put("q", "query");
    Assertions.assertEquals("/root/100?q=query", oURLPathBuilder.createRequestPath(parameters));
    Assertions.assertEquals("/root/100", oURLPathBuilder.createPathString(parameters));
  }

  @Test
  public void testQueryVarParamWriter() {
    boolean status = true;

    Parameter parameter = new QueryParameter();
    parameter.setSchema(new Schema<>());
    RestParam restParam = new RestParam(null, parameter, String.class);
    RestParam spy = Mockito.spy(restParam);
    Mockito.when(spy.getParamName()).thenReturn("queryVar");
    QueryVarParamWriter writer = new QueryVarParamWriter(spy);
    try {
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("queryVar", "T");
      verify(writer, parameters, "?queryVar=T");
      parameters.put("queryVar", null);
      verify(writer, parameters, "");
      parameters.put("queryVar", new String[] {"a", "b"});
      verify(writer, parameters, "?queryVar=a&queryVar=b");
      parameters.put("queryVar", new String[] {"a", null, "b"});
      verify(writer, parameters, "?queryVar=a&queryVar=b");
      parameters.put("queryVar", Arrays.asList("Lars", "Simon"));
      verify(writer, parameters, "?queryVar=Lars&queryVar=Simon");
      parameters.put("queryVar", "测试");
      verify(writer, parameters, "?queryVar=%E6%B5%8B%E8%AF%95");
      parameters.put("queryVar", "a b");
      verify(writer, parameters, "?queryVar=a+b");
      parameters.put("queryVar", "a+b");
      verify(writer, parameters, "?queryVar=a%2Bb");
    } catch (Exception e) {
      status = false;
    }
    Assertions.assertTrue(status);
  }

  private void verify(QueryVarParamWriter writer, Map<String, Object> args, String expect) throws Exception {
    URLPathStringBuilder sb = new URLPathStringBuilder();
    writer.write(sb, args);
    Assertions.assertEquals(expect, sb.build());
  }
}
