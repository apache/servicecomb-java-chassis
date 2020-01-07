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

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.path.PathRegExp;
import org.apache.servicecomb.common.rest.definition.path.QueryVarParamWriter;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import mockit.Mock;
import mockit.MockUp;

public class TestPath {

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testPathRegExp() throws Exception {
    PathRegExp oPathRegExp = new PathRegExp("//{test}//");
    Assert.assertEquals(1, oPathRegExp.getGroupCount());
    Assert.assertEquals(0, oPathRegExp.getGroupWithRegExpCount());
    PathRegExp oSecondPathRegExp = new PathRegExp("{[^/:]+?}");
    Assert.assertEquals(1, oSecondPathRegExp.getGroupCount());
    Assert.assertEquals(1, oSecondPathRegExp.getGroupWithRegExpCount());
    Assert.assertEquals("test/", PathRegExp.ensureEndWithSlash("test/"));
    Assert.assertEquals("test/", PathRegExp.ensureEndWithSlash("test"));
    Assert.assertEquals(null, oSecondPathRegExp.match("{test/test}", null));
    Assert.assertEquals("(]+?)/(.*)", (oSecondPathRegExp.toString()));
    Assert.assertEquals(false, oSecondPathRegExp.isStaticPath());
    Assert.assertEquals(0, oSecondPathRegExp.getStaticCharCount());
    Assert.assertNotEquals(null, (oPathRegExp.match("//{test}//", new HashMap<>())));
    // Error Scenarios
    new PathRegExp("//{test \t}//");
    // Error Scenarios for double {{
    try {
      new PathRegExp("//{test{");
      fail("an exception is expected!");
    } catch (Exception e) {
      Assert.assertEquals(true, e.getMessage().contains("A variable must not contain an extra"));
    }
    // Error Scenarios for illegal }}
    try {
      new PathRegExp("//}");
      fail("an exception is expected!");
    } catch (Exception e) {
      Assert.assertEquals(true, e.getMessage().contains("is only allowed as"));
    }
    // Error Scenarios for illegal ;
    try {
      new PathRegExp("//;");
      fail("an exception is expected!");
    } catch (Exception e) {
      Assert.assertEquals(true, e.getMessage().contains("matrix parameters are not allowed in"));
    }

    // Error Scenarios for NO } ;
    try {
      new PathRegExp("//{test");
      fail("an exception is expected!");
    } catch (Exception e) {
      Assert.assertEquals(true, e.getMessage().contains("No '}' found after"));
    }
  }

  @Test
  public void testUrlPathBuilder() throws Exception {
    Map<String, RestParam> paramMap = new HashMap<>();

    Parameter pathParameter = new PathParameter();
    pathParameter.setName("id");
    RestParam oRestParam = new RestParam(pathParameter, int.class);
    paramMap.put(oRestParam.getParamName(), oRestParam);

    Parameter queryParameter = new QueryParameter();
    queryParameter.setName("q");
    oRestParam = new RestParam(queryParameter, String.class);
    paramMap.put(oRestParam.getParamName(), oRestParam);

    URLPathBuilder oURLPathBuilder = new URLPathBuilder("/root/{id}", paramMap);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("id", 100);
    parameters.put("q", "query");
    Assert.assertEquals("/root/100?q=query", oURLPathBuilder.createRequestPath(parameters));
    Assert.assertEquals("/root/100", oURLPathBuilder.createPathString(parameters));
  }

  @Test
  public void testQueryVarParamWriter() {
    boolean status = true;
    new MockUp<RestParam>() {
      @Mock
      public String getParamName() {
        return "queryVar";
      }
    };
    new MockUp<QueryVarParamWriter>() {
      @Mock
      protected Object getParamValue(Object[] args) {
        return args[0];
      }
    };

    Parameter parameter = new QueryParameter();
    QueryVarParamWriter writer = new QueryVarParamWriter(new RestParam(parameter, String.class));
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
    Assert.assertTrue(status);
  }

  private void verify(QueryVarParamWriter writer, Map<String, Object> args, String expect) throws Exception {
    URLPathStringBuilder sb = new URLPathStringBuilder();
    writer.write(sb, args);
    Assert.assertEquals(expect, sb.build());
  }
}
