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

import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.junit.Assert;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;

public class PathVarParamWriterTest {

  @Test
  public void writePlainPath() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();

    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    pathVarParamWriter.write(pathBuilder, new Object[] {"abc"});
    Assert.assertEquals("abc", pathBuilder.build());
  }

  @Test
  public void writePathWithSpace() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();

    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    pathVarParamWriter.write(pathBuilder, new String[] {"a 20bc"});
    Assert.assertEquals("a%2020bc", pathBuilder.build());
  }

  @Test
  public void writePathWithPercentage() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();
    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    pathBuilder.appendPath("/api/");
    pathVarParamWriter.write(pathBuilder, new String[] {"a%%bc"});
    Assert.assertEquals("/api/a%25%25bc", pathBuilder.build());
  }

  @Test
  public void writePathParamWithSlash() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();
    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    pathBuilder.appendPath("/api/");
    pathVarParamWriter.write(pathBuilder, new String[] {"a/bc"});
    Assert.assertEquals("/api/a%2Fbc", pathBuilder.build());
  }

  @Test
  public void writeIntegerParam() throws Exception {
    PathVarParamWriter pathVarParamWriter = createPathVarParamWriter();
    URLPathStringBuilder pathBuilder = new URLPathStringBuilder();
    pathVarParamWriter.write(pathBuilder, new Integer[] {12});
    Assert.assertEquals("12", pathBuilder.build());
  }

  private PathVarParamWriter createPathVarParamWriter() {
    RestParam restParam = new MockUp<RestParam>() {
      @Mock
      Object getValue(Object[] args) {
        return args[0];
      }
    }.getMockInstance();
    return new PathVarParamWriter(restParam);
  }
}