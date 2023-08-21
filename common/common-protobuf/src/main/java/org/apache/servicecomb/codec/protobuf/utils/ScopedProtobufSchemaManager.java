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

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.codec.protobuf.internal.converter.SwaggerToProtoGenerator;
import org.apache.servicecomb.codec.protobuf.schema.SchemaToProtoGenerator;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;
import org.apache.servicecomb.swagger.SwaggerUtils;

import io.protostuff.compiler.model.Proto;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Manage swagger -> protoBuffer mappings.
 *
 * This class have the same lifecycle as MicroserviceMeta, so we need to create an instance
 * for each MicroserviceMeta.
 */
public class ScopedProtobufSchemaManager {
  static class SchemaKey {
    String schemaId;

    Schema<?> schema;

    int hashCode = -1;

    SchemaKey(String schemaId, Schema<?> schema) {
      this.schemaId = schemaId;
      this.schema = schema;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SchemaKey other = (SchemaKey) o;
      return StringUtils.equals(schemaId, other.schemaId)
          && SwaggerUtils.schemaEquals(schema, other.schema);
    }

    @Override
    public int hashCode() {
      if (hashCode != -1) {
        return hashCode;
      }
      hashCode = schemaId.hashCode() ^ SwaggerUtils.schemaHashCode(schema);
      return hashCode;
    }
  }

  // Because this class belongs to each SchemaMeta, the key is the schema id.
  private final Map<String, ProtoMapper> mapperCache = new ConcurrentHashMapEx<>();

  private final Map<SchemaKey, ProtoMapper> schemaMapperCache = new ConcurrentHashMapEx<>();

  public ScopedProtobufSchemaManager() {

  }

  /**
   * get the ProtoMapper from Swagger
   */
  public ProtoMapper getOrCreateProtoMapper(SchemaMeta schemaMeta) {
    return mapperCache.computeIfAbsent(schemaMeta.getSchemaId(), key -> {
      OpenAPI swagger = schemaMeta.getSwagger();
      SwaggerToProtoGenerator generator = new SwaggerToProtoGenerator(schemaMeta.getMicroserviceQualifiedName(),
          swagger);
      Proto proto = generator.convert();
      ProtoMapperFactory protoMapperFactory = new ProtoMapperFactory();
      return protoMapperFactory.create(proto);
    });
  }

  /**
   * get the ProtoMapper from Schema
   */
  public ProtoMapper getOrCreateProtoMapper(OpenAPI openAPI, String schemaId, String name, Schema<?> schema) {
    SchemaKey schemaKey = new SchemaKey(schemaId, schema);
    return schemaMapperCache.computeIfAbsent(schemaKey, key -> {
      SchemaToProtoGenerator generator = new SchemaToProtoGenerator("scb.schema", openAPI,
          key.schema, name);
      Proto proto = generator.convert();
      ProtoMapperFactory protoMapperFactory = new ProtoMapperFactory();
      return protoMapperFactory.create(proto);
    });
  }
}
