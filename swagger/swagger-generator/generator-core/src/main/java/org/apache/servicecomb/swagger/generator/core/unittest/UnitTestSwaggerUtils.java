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

package org.apache.servicecomb.swagger.generator.core.unittest;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

public final class UnitTestSwaggerUtils {
  private static ObjectWriter writer = Yaml.pretty();

  private UnitTestSwaggerUtils() {
  }

  public static String loadExpect(String resPath) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(resPath);
    if (url == null) {
      return "can not found res " + resPath;
    }

    try {
      return IOUtils.toString(url);
    } catch (IOException e) {
      return e.getMessage();
    }
  }

  public static String pretty(Swagger swagger) {
    try {
      return writer.writeValueAsString(swagger);
    } catch (JsonProcessingException e) {
      throw new Error(e);
    }
  }

  public static Swagger parse(String content) {
    try {
      return Yaml.mapper().readValue(content, Swagger.class);
    } catch (Exception e) {
      return new Swagger();
      //            throw new Error(e);
    }
  }

  public static SwaggerGenerator testSwagger(String resPath, Class<?> cls, String... methods) {
    SwaggerGenerator generator = SwaggerGenerator.create(cls);
    generator.replaceMethodWhiteList(methods);
    generator.getSwaggerGeneratorFeature().setPackageName("gen.cse.ms.ut");

    Swagger swagger = generator.generate();
    String schema = pretty(swagger);

    String expectSchema = loadExpect(resPath).replace("\r\n", "\n");
    int offset = expectSchema.indexOf("---\nswagger: \"2.0\"");
    if (offset > 0) {
      expectSchema = expectSchema.substring(offset);
    }

    if (!Objects.equals(expectSchema, schema)) {
      Assert.assertEquals(expectSchema, schema);
    }

    return generator;
  }

  public static Throwable getException(Class<?> cls, String... methods) {
    try {
      SwaggerGenerator generator = SwaggerGenerator.create(cls);
      generator.replaceMethodWhiteList(methods);

      generator.generate();
    } catch (Throwable e) {
      return e;
    }

    // 不允许成功
    Assert.assertEquals("not allowed run to here", "run to here");
    return null;
  }

  public static void testException(String expectMsgLevel1, String expectMsgLevel2, String expectMsgLevel3, Class<?> cls,
      String... methods) {
    Throwable exception = getException(cls, methods);
    Assert.assertEquals(expectMsgLevel1, exception.getMessage());
    Assert.assertEquals(expectMsgLevel2, exception.getCause().getMessage());
    Assert.assertEquals(expectMsgLevel3, exception.getCause().getCause().getMessage());
  }

  public static void testException(String expectMsgLevel1, String expectMsgLevel2, Class<?> cls, String... methods) {
    Throwable exception = getException(cls, methods);
    Assert.assertEquals(expectMsgLevel1, exception.getMessage());
    Assert.assertEquals(expectMsgLevel2, exception.getCause().getMessage());
  }

  public static void testException(String expectMsg, Class<?> cls, String... methods) {
    Throwable exception = getException(cls, methods);
    Assert.assertEquals(expectMsg, exception.getMessage());
  }
}
