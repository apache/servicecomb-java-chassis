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

package org.apache.servicecomb.codec.protobuf.utils;

import java.util.Map;

import org.apache.servicecomb.codec.protobuf.internal.converter.SwaggerToProtoGenerator;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;

import io.protostuff.compiler.model.Proto;
import io.swagger.models.Swagger;

/**
 * Manage swagger -> protoBuffer mappings.
 *
 * This class have the same lifecycle as MicroserviceMeta, so we need to create an instance
 * for each MicroserviceMeta.
 */
public class ScopedProtobufSchemaManager {
  // Because this class belongs to each SchemaMeta, the key is the schema id.
  private final Map<String, ProtoMapper> mapperCache = new ConcurrentHashMapEx<>();

  public ScopedProtobufSchemaManager() {

  }

  /**
   * get the ProtoMapper from Swagger
   */
  public ProtoMapper getOrCreateProtoMapper(SchemaMeta schemaMeta) {
    return mapperCache.computeIfAbsent(schemaMeta.getSchemaId(), key -> {
      Swagger swagger = schemaMeta.getSwagger();
      SwaggerToProtoGenerator generator = new SwaggerToProtoGenerator(schemaMeta.getMicroserviceQualifiedName(),
          swagger);
      Proto proto = generator.convert();
      ProtoMapperFactory protoMapperFactory = new ProtoMapperFactory();
      return protoMapperFactory.create(proto);
    });
  }
}
