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

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.junit.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

public final class UnitTestSwaggerUtils {
  private static ObjectWriter writer = Yaml.pretty();

  private static CompositeSwaggerGeneratorContext compositeContext = new CompositeSwaggerGeneratorContext();

  private UnitTestSwaggerUtils() {
  }

  public static SwaggerGenerator generateSwagger(Class<?> cls) {
    return generateSwagger(Thread.currentThread().getContextClassLoader(), cls);
  }

  public static SwaggerGenerator generateSwagger(ClassLoader classLoader, Class<?> cls) {
    SwaggerGeneratorContext context = compositeContext.selectContext(cls);
    SwaggerGenerator generator = new SwaggerGenerator(context, cls);
    generator.setClassLoader(classLoader);
    generator.generate();

    return generator;
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

  public static SwaggerGenerator testSwagger(ClassLoader classLoader, String resPath, SwaggerGeneratorContext context,
      Class<?> cls,
      String... methods) {
    SwaggerGeneratorForTest generator = new SwaggerGeneratorForTest(context, cls);
    generator.setClassLoader(classLoader);
    generator.replaceMethods(methods);

    Swagger swagger = generator.generate();

    String expectSchema = loadExpect(resPath);
    Swagger expectSwagger = parse(expectSchema);

    String schema = pretty(swagger);
    swagger = parse(schema);

    if (swagger != null && !swagger.equals(expectSwagger)) {
      Assert.assertEquals(expectSchema, schema);
    }

    return generator;
  }

  public static Throwable getException(SwaggerGeneratorContext context, Class<?> cls,
      String... methods) {
    try {
      SwaggerGeneratorForTest generator = new SwaggerGeneratorForTest(context, cls);
      generator.replaceMethods(methods);

      generator.generate();
    } catch (Throwable e) {
      return e;
    }

    // 不允许成功
    Assert.assertEquals("not allowed run to here", "run to here");
    return null;
  }

  public static void testException(String expectMsgLevel1, String expectMsgLevel2, SwaggerGeneratorContext context,
      Class<?> cls,
      String... methods) {
    Throwable exception = getException(context, cls, methods);
    Assert.assertEquals(expectMsgLevel1, exception.getMessage());
    Assert.assertEquals(expectMsgLevel2, exception.getCause().getMessage());
  }

  public static void testException(String expectMsg, SwaggerGeneratorContext context, Class<?> cls,
      String... methods) {
    Throwable exception = getException(context, cls, methods);
    Assert.assertEquals(expectMsg, exception.getMessage());
  }
}
