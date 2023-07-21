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
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

public final class UnitTestSwaggerUtils {
  private static final ObjectWriter writer = Yaml.pretty();

  private UnitTestSwaggerUtils() {
  }

  public static String loadExpect(String resPath) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(resPath);
    if (url == null) {
      return "can not found res " + resPath;
    }

    try {
      return IOUtils.toString(url, StandardCharsets.UTF_8);
    } catch (IOException e) {
      return e.getMessage();
    }
  }

  public static String pretty(OpenAPI swagger) {
    try {
      return writer.writeValueAsString(swagger);
    } catch (JsonProcessingException e) {
      throw new Error(e);
    }
  }

  public static OpenAPI parse(String content) {
    try {
      return Yaml.mapper().readValue(content, OpenAPI.class);
    } catch (Exception e) {
      return new OpenAPI();
      //            throw new Error(e);
    }
  }

  public static SwaggerGenerator testSwagger(String resPath, Class<?> cls, String... methods) {
    SwaggerGenerator generator = SwaggerGenerator.create(cls);
    generator.replaceMethodWhiteList(methods);
    generator.getSwaggerGeneratorFeature().setPackageName("gen.cse.ms.ut");

    OpenAPI swagger = generator.generate();
    String schema = pretty(swagger).trim();

    String expectSchema = loadExpect(resPath).replace("\r\n", "\n").trim();
    int offset = expectSchema.indexOf("---\nopenapi: 3.0.1");
    if (offset > 0) {
      expectSchema = expectSchema.substring(offset + 4);
    }

    if (!Objects.equals(expectSchema, schema)) {
      Assertions.assertEquals(expectSchema, schema);
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
    Assertions.assertEquals("not allowed run to here", "run to here");
    return null;
  }

  public static void testException(String expectMsgLevel1, String expectMsgLevel2, String expectMsgLevel3, Class<?> cls,
      String... methods) {
    Throwable exception = getException(cls, methods);
    Assertions.assertEquals(expectMsgLevel1, exception.getMessage());
    Assertions.assertEquals(expectMsgLevel2, exception.getCause().getMessage());
    Assertions.assertEquals(expectMsgLevel3, exception.getCause().getCause().getMessage());
  }

  public static void testException(String expectMsgLevel1, String expectMsgLevel2, Class<?> cls, String... methods) {
    Throwable exception = getException(cls, methods);
    Assertions.assertEquals(expectMsgLevel1, exception.getMessage());
    Assertions.assertEquals(expectMsgLevel2, exception.getCause().getMessage());
  }

  public static void testException(String expectMsg, Class<?> cls, String... methods) {
    Throwable exception = getException(cls, methods);
    Assertions.assertEquals(expectMsg, exception.getMessage());
  }
}
