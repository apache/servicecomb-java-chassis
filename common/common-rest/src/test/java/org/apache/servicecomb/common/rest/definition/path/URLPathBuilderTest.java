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

package org.apache.servicecomb.common.rest.definition.path;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestParam;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;

public class URLPathBuilderTest {
  @Test
  public void testNormal() throws Exception {
    Map<String, RestParam> paramMap = new LinkedHashMap<>();
    addParam("p0", int.class, PathParameter::new, paramMap);
    addParam("p1", String.class, PathParameter::new, paramMap);
    addParam("q0", int.class, QueryParameter::new, paramMap);
    addParam("q1", String.class, QueryParameter::new, paramMap);

    URLPathBuilder urlPathBuilder = new URLPathBuilder("/path/{p0}/and/{p1}", paramMap);
    Object[] args = {10, "abcPath", 11, "queryABC"};
    Assert.assertEquals("/path/10/and/abcPath?q0=11&q1=queryABC",
        urlPathBuilder.createRequestPath(args));
    Assert.assertEquals("/path/10/and/abcPath",
        urlPathBuilder.createPathString(args));
  }

  @Test
  public void testEncode() throws Exception {
    Map<String, RestParam> paramMap = new LinkedHashMap<>();
    addParam("p", String.class, PathParameter::new, paramMap);
    addParam("q", String.class, QueryParameter::new, paramMap);

    URLPathBuilder urlPathBuilder = new URLPathBuilder("/path/{p}", paramMap);
    Object[] args = {"ab%% %cd%", "ab%% %cd%"};
    Assert.assertEquals("/path/ab%25%25%20%25cd%25?q=ab%25%25+%25cd%25",
        urlPathBuilder.createRequestPath(args));
    Assert.assertEquals("/path/ab%25%25%20%25cd%25",
        urlPathBuilder.createPathString(args));
  }

  @Test
  public void testMultiQuery() throws Exception {
    Map<String, RestParam> paramMap = new LinkedHashMap<>();
    addParam("strArr", String[].class, QueryParameter::new, paramMap);
    addParam("intArr", int[].class, QueryParameter::new, paramMap);

    URLPathBuilder urlPathBuilder = new URLPathBuilder("/path", paramMap);
    Object[] args = new Object[] {
        new Object[] {"a", "b", "c"},
        new Object[] {1, 2, 3}
    };
    Assert.assertEquals("/path?strArr=a&strArr=b&strArr=c&intArr=1&intArr=2&intArr=3",
        urlPathBuilder.createRequestPath(args));
    args = new Object[] {
        new Object[] {},
        new Object[] {1, 2, 3}
    };
    Assert.assertEquals("/path?intArr=1&intArr=2&intArr=3",
        urlPathBuilder.createRequestPath(args));
    args = new Object[] {
        new Object[] {"a", "b", "c"},
        new Object[] {}
    };
    Assert.assertEquals("/path?strArr=a&strArr=b&strArr=c",
        urlPathBuilder.createRequestPath(args));
    args = new Object[] {
        new Object[] {},
        new Object[] {}
    };
    Assert.assertEquals("/path",
        urlPathBuilder.createRequestPath(args));
  }

  private void addParam(String paramName, Type paramType,
      ParameterConstructor constructor, Map<String, RestParam> paramMap) {
    Parameter parameter = constructor.construct();
    parameter.setName(paramName);
    paramMap.put(paramName, new RestParam(paramMap.size(), parameter, paramType));
  }

  static interface ParameterConstructor {
    Parameter construct();
  }
}
