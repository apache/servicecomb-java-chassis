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
package org.apache.servicecomb.foundation.protobuf;

import org.apache.servicecomb.foundation.protobuf.internal.bean.BeanDescriptorManager;
import org.apache.servicecomb.foundation.protobuf.internal.parser.ProtoParser;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.protostuff.compiler.model.Proto;

public class ProtoMapperFactory {
  // 1.to support "any" type
  // 2.to find bean properties
  private ObjectMapper jsonMapper = new ObjectMapper();

  private BeanDescriptorManager beanDescriptorManager;

  private ProtoParser protoParser = new ProtoParser();

  public ProtoMapperFactory() {
    jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    jsonMapper.setSerializationInclusion(Include.NON_NULL);
//    jsonMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);

    beanDescriptorManager = new BeanDescriptorManager(jsonMapper.getSerializationConfig());
  }

  public BeanDescriptorManager getBeanDescriptorManager() {
    return beanDescriptorManager;
  }

  public ProtoMapper createFromContent(String content) {
    Proto proto = protoParser.parseFromContent(content);
    return create(proto);
  }

  public ProtoMapper createFromName(String name) {
    Proto proto = protoParser.parse(name);
    return create(proto);
  }

  public ProtoMapper create(Proto proto) {
    return new ProtoMapper(jsonMapper, beanDescriptorManager, proto);
  }
}
