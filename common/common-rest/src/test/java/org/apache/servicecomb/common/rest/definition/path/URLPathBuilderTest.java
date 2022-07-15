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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestParam;
import org.junit.jupiter.api.Assertions;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import org.junit.jupiter.api.Test;

public class URLPathBuilderTest {
  @Test
  public void testNormal() throws Exception {
    Map<String, RestParam> paramMap = new LinkedHashMap<>();
    addParam("p0", int.class, PathParameter::new, paramMap);
    addParam("p1", String.class, PathParameter::new, paramMap);
    addParam("q0", int.class, QueryParameter::new, paramMap);
    addParam("q1", String.class, QueryParameter::new, paramMap);

    URLPathBuilder urlPathBuilder = new URLPathBuilder("/path/{p0}/and/{p1}", paramMap);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("p0", 10);
    parameters.put("p1", "abcPath");
    parameters.put("q0", 11);
    parameters.put("q1", "queryABC");
    Assertions.assertEquals("/path/10/and/abcPath?q0=11&q1=queryABC",
        urlPathBuilder.createRequestPath(parameters));
    Assertions.assertEquals("/path/10/and/abcPath",
        urlPathBuilder.createPathString(parameters));
  }

  @Test
  public void testEncode() throws Exception {
    Map<String, RestParam> paramMap = new LinkedHashMap<>();
    addParam("p", String.class, PathParameter::new, paramMap);
    addParam("q", String.class, QueryParameter::new, paramMap);

    URLPathBuilder urlPathBuilder = new URLPathBuilder("/path/{p}", paramMap);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("p", "ab%% %cd%");
    parameters.put("q", "ab%% %cd%");
    Assertions.assertEquals("/path/ab%25%25%20%25cd%25?q=ab%25%25+%25cd%25",
        urlPathBuilder.createRequestPath(parameters));
    Assertions.assertEquals("/path/ab%25%25%20%25cd%25",
        urlPathBuilder.createPathString(parameters));
  }

  @Test
  public void testMultiQuery() throws Exception {
    Map<String, RestParam> paramMap = new LinkedHashMap<>();
    addParam("strArr", String[].class, QueryParameter::new, paramMap);
    addParam("intArr", int[].class, QueryParameter::new, paramMap);

    URLPathBuilder urlPathBuilder = new URLPathBuilder("/path", paramMap);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("strArr", new Object[] {"a", "b", "c"});
    parameters.put("intArr", new Object[] {1, 2, 3});
    Assertions.assertEquals("/path?strArr=a&strArr=b&strArr=c&intArr=1&intArr=2&intArr=3",
        urlPathBuilder.createRequestPath(parameters));
    parameters.put("strArr", new Object[] {});
    parameters.put("intArr", new Object[] {1, 2, 3});
    Assertions.assertEquals("/path?intArr=1&intArr=2&intArr=3",
        urlPathBuilder.createRequestPath(parameters));
    parameters.put("strArr", new Object[] {"a", "b", "c"});
    parameters.put("intArr", new Object[] {});
    Assertions.assertEquals("/path?strArr=a&strArr=b&strArr=c",
        urlPathBuilder.createRequestPath(parameters));
    parameters.put("strArr", new Object[] {});
    parameters.put("intArr", new Object[] {});
    Assertions.assertEquals("/path",
        urlPathBuilder.createRequestPath(parameters));
  }

  @Test
  public void testRegexPathParam() throws Exception {
    Map<String, RestParam> paramMap = new LinkedHashMap<>();
    addParam("p0", int.class, PathParameter::new, paramMap);
    addParam("p1", String.class, PathParameter::new, paramMap);
    addParam("q0", int.class, QueryParameter::new, paramMap);
    addParam("q1", String.class, QueryParameter::new, paramMap);

    URLPathBuilder urlPathBuilder = new URLPathBuilder("/path/{p0 : .*}/and/{p1:.*}", paramMap);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("p0", 10);
    parameters.put("p1", "abcPath");
    parameters.put("q0", 11);
    parameters.put("q1", "queryABC");
    Assertions.assertEquals("/path/10/and/abcPath?q0=11&q1=queryABC",
        urlPathBuilder.createRequestPath(parameters));
    Assertions.assertEquals("/path/10/and/abcPath",
        urlPathBuilder.createPathString(parameters));
  }

  private void addParam(String paramName, Type paramType,
      ParameterConstructor constructor, Map<String, RestParam> paramMap) {
    Parameter parameter = constructor.construct();
    parameter.setName(paramName);
    paramMap.put(paramName, new RestParam(parameter, paramType));
  }

  interface ParameterConstructor {
    Parameter construct();
  }
}
