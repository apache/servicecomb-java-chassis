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
package org.apache.servicecomb.foundation.protobuf.internal;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

public class TestMethodWrap {
  protected static ProtoMapperFactory factory = new ProtoMapperFactory();

  protected static ProtoMapper methodProtoMapper = factory.createFromName("method.proto");

  @Test
  public void arguments() throws IOException {
    // serialize
//    RootSerializer argumentsSerializer = methodProtoMapper
//        .createArgumentsRootSerializer("RequestWrap", boolean.class, Integer[].class);
//    RootSerializer protoSerializer = methodProtoMapper.createRootSerializer("Request", Map.class);
//
//    Integer[] intArr = new Integer[] {1, 2};
//
//    Map<String, Object> request = new LinkedHashMap<>();
//    request.put("boolValue", true);
//    request.put("iArr", intArr);
//    byte[] bytes = protoSerializer.serialize(request);
//
//    Assert.assertArrayEquals(bytes, argumentsSerializer.serialize(new Object[] {true, intArr}));

    // deserialize
//    Object[] arguments = methodProtoMapper
//        .createArgumentsRootDeserializer(
//            "RequestWrap",
//            boolean.class,
//            TypeFactory.defaultInstance().constructParametricType(List.class, Integer.class))
//        .deserialize(bytes);
//    Assert.assertTrue((Boolean) arguments[0]);
//    Assert.assertEquals(Arrays.asList(intArr), arguments[1]);
  }

  @Test
  public void response() throws IOException {
    Type type = TypeFactory.defaultInstance().constructCollectionType(List.class, Integer.class);
    // serialize
    RootSerializer responseSerializer = methodProtoMapper.createRootSerializer("ResponseWrap", type);
    RootSerializer protoSerializer = methodProtoMapper.createRootSerializer("Response", PropertyWrapper.class);

    List<Integer> list = Arrays.asList(1, 2);

    Map<String, Object> response = new HashMap<>();
    response.put("value", list);
    byte[] bytes = protoSerializer.serialize(response);

    Assert.assertArrayEquals(bytes, responseSerializer.serialize(list));

    @SuppressWarnings("unchecked")
    PropertyWrapper<Object> propertyWrapper = (PropertyWrapper<Object>) methodProtoMapper
        .createRootDeserializer("Response", PropertyWrapper.class)
        .deserialize(bytes);
    Assert.assertEquals(list, propertyWrapper.getValue());

    Assert.assertEquals(list,
        methodProtoMapper.createPropertyRootDeserializer("ResponseWrap", type).deserialize(bytes));
  }
}
