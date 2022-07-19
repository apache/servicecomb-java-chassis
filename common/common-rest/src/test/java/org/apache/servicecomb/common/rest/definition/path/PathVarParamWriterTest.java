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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PathVarParamWriterTest {

  @Test
  public void writePlainPath() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();

    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("test", "abc");
    pathVarParamWriter.write(pathBuilder, parameters);
    Assertions.assertEquals("abc", pathBuilder.build());
  }

  @Test
  public void writePathWithSpace() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();

    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("test", "a 20bc");
    pathVarParamWriter.write(pathBuilder, parameters);
    Assertions.assertEquals("a%2020bc", pathBuilder.build());
  }

  @Test
  public void writePathWithPercentage() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();
    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    pathBuilder.appendPath("/api/");
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("test", "a%%bc");
    pathVarParamWriter.write(pathBuilder, parameters);
    Assertions.assertEquals("/api/a%25%25bc", pathBuilder.build());
  }

  @Test
  public void writePathParamWithSlash() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();
    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    pathBuilder.appendPath("/api/");
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("test", "a/bc");
    pathVarParamWriter.write(pathBuilder, parameters);
    Assertions.assertEquals("/api/a%2Fbc", pathBuilder.build());
  }

  @Test
  public void writeIntegerParam() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();
    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("test", 12);
    pathVarParamWriter.write(pathBuilder, parameters);
    Assertions.assertEquals("12", pathBuilder.build());
  }

  private PathVarParamWriter createPathVarParamWriter() {
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(restParam.getParamName()).thenReturn("test");
    return new PathVarParamWriter(restParam);
  }
}
