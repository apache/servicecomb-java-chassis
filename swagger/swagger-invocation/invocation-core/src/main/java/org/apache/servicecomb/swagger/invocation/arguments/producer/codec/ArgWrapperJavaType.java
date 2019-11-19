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
package org.apache.servicecomb.swagger.invocation.arguments.producer.codec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapperUtils;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.util.Json;

public class ArgWrapperJavaType extends SimpleType {
  private static final long serialVersionUID = 964882360361204479L;

  class ArgInfo {
    JavaType javaType;

    JsonDeserializer<Object> deserializer;

    public ArgInfo(Type type) {
      this.javaType = TypeFactory.defaultInstance().constructType(type);
    }
  }

  private Map<String, ArgInfo> argInfos = new HashMap<>();

  public ArgWrapperJavaType() {
    super(Object.class);
  }

  public void addProperty(String name, Type type) {
    argInfos.put(name, new ArgInfo(type));
  }

  public Map<String, Object> readValue(ObjectMapper mapper, String json) throws IOException {
    Map<String, Object> args = new LinkedHashMap<>();

    JsonParser jp = mapper.getFactory().createParser(json);
    DeserializationContext deserializationContext = ObjectMapperUtils.createDeserializationContext(mapper, jp);

    jp.nextToken();
    for (String fieldName = jp.nextFieldName(); fieldName != null; fieldName = jp.nextFieldName()) {
      jp.nextToken();
      ArgInfo argInfo = argInfos.get(fieldName);
      if (argInfo == null) {
        continue;
      }

      if (argInfo.deserializer == null) {
        argInfo.deserializer = deserializationContext.findRootValueDeserializer(argInfo.javaType);
      }

      args.put(fieldName, argInfo.deserializer.deserialize(jp, deserializationContext));
    }

    return args;
  }

  public static void main(String[] _args) throws IOException {
    Map<String, Object> map = new HashMap<>();
    map.put("date", new Date());
    map.put("num", 1L);

    ArgWrapperJavaType argWrapperJavaType = new ArgWrapperJavaType();
    argWrapperJavaType.addProperty("date", Date.class);
    argWrapperJavaType.addProperty("num", Long.class);

    String json = Json.pretty(map);

    Map<String, Object> args = argWrapperJavaType.readValue(Json.mapper(), json);
    System.out.println(args);
  }
}
