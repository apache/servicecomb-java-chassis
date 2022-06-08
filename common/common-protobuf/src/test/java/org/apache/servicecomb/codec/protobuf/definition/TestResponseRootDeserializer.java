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
package org.apache.servicecomb.codec.protobuf.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.internal.ProtoConst;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Test;

class Model {

}

@SuppressWarnings({"rawtypes"})
public class TestResponseRootDeserializer {
  @Test
  public void testNeedConvert() {
    Assertions.assertEquals(SimpleType.constructUnsafe(Object.class), ProtoConst.OBJECT_TYPE);
    Assertions.assertFalse(ResponseRootDeserializer.needConvert(1, TypeFactory.defaultInstance().constructType(int.class)));
    Assertions.assertFalse(ResponseRootDeserializer.needConvert(1, TypeFactory.defaultInstance().constructType(Integer.class)));
    Assertions.assertFalse(ResponseRootDeserializer
            .needConvert(1, TypeFactory.defaultInstance().constructType(int.class)));
    Assertions.assertFalse(ResponseRootDeserializer
            .needConvert(1, TypeFactory.defaultInstance().constructType(Integer.class)));
    Assertions.assertTrue(ResponseRootDeserializer
            .needConvert(new HashMap<>(), TypeFactory.defaultInstance().constructType(Model.class)));
    Assertions.assertFalse(ResponseRootDeserializer
            .needConvert(new Model(), TypeFactory.defaultInstance().constructType(Model.class)));
    Assertions.assertFalse(ResponseRootDeserializer
            .needConvert(new Model(), TypeFactory.defaultInstance().constructType(Object.class)));
    List<Model> modelList = new ArrayList<>();
    List<Map> modelMapList = new ArrayList<>();
    Assertions.assertTrue(ResponseRootDeserializer
            .needConvert(modelMapList, TypeFactory.defaultInstance().constructType(new TypeReference<List<Model>>() {
            })));
    // This case should be false, however it is not exists in real applications, for simpler, take it true.
    Assertions.assertTrue(ResponseRootDeserializer
            .needConvert(modelList, TypeFactory.defaultInstance().constructType(new TypeReference<List<Model>>() {
            })));
  }
}
