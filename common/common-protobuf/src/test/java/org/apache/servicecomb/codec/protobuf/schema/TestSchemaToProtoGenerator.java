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
package org.apache.servicecomb.codec.protobuf.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.servicecomb.codec.protobuf.internal.converter.ProtoToStringGenerator;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.protostuff.compiler.model.Proto;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import jakarta.ws.rs.core.MediaType;

//CHECKSTYLE:OFF
@SuppressWarnings("unused")
public class TestSchemaToProtoGenerator {
  @Test
  public void test_string_schema_is_correct() {
    OpenAPI openAPI = new OpenAPI();
    StringSchema schema = new StringSchema();
    SchemaToProtoGenerator generator =
        new SchemaToProtoGenerator("test.string", openAPI, schema, "input");
    Proto proto = generator.convert();
    assertEquals("""
        syntax = "proto3";
        package test.string;

        //@WrapProperty
        message input {
          string value = 1;
        }

        """, new ProtoToStringGenerator(proto).protoToString());
  }

  @Test
  public void test_object_schema_is_correct() {
    OpenAPI openAPI = new OpenAPI();

    ObjectSchema schema = new ObjectSchema();
    schema.setName("User");
    schema.addProperty("name", new StringSchema());
    openAPI.setComponents(new Components());
    openAPI.getComponents().addSchemas("User", schema);

    ObjectSchema ref = new ObjectSchema();
    ref.set$ref(Components.COMPONENTS_SCHEMAS_REF + "User");

    SchemaToProtoGenerator generator =
        new SchemaToProtoGenerator("test.object", openAPI, ref, "input");
    Proto proto = generator.convert();
    assertEquals("""
        syntax = "proto3";
        package test.object;

        message User {
          string name = 1;
        }

        //@WrapProperty
        message input {
          User value = 1;
        }

        """, new ProtoToStringGenerator(proto).protoToString());
  }

  static class Model {
    public String name;

    public int age;
  }

  interface SpringMvcSchema {
    @PostMapping("/testInt")
    int testInt(@RequestBody int param);

    @PostMapping("/testModel")
    Model testModel(@RequestBody Model model);
  }

  @Test
  public void test_springmvc_int_schema_correct() {
    SpringmvcSwaggerGenerator generator = new SpringmvcSwaggerGenerator(SpringMvcSchema.class);
    OpenAPI openAPI = generator.generate();

    SchemaToProtoGenerator protoGenerator =
        new SchemaToProtoGenerator("test.int", openAPI,
            openAPI.getPaths().get("/testInt").getPost()
                .getRequestBody().getContent().get(MediaType.APPLICATION_JSON)
                .getSchema(), "testIntRequest");
    Proto proto = protoGenerator.convert();
    assertEquals("""
        syntax = "proto3";
        package test.int;
                
        //@WrapProperty
        message testIntRequest {
          int32 value = 1;
        }
        """.trim(), new ProtoToStringGenerator(proto).protoToString().trim());

    protoGenerator =
        new SchemaToProtoGenerator("test.int", openAPI,
            openAPI.getPaths().get("/testInt").getPost()
                .getResponses().get("200").getContent().get(MediaType.APPLICATION_JSON)
                .getSchema(), "testIntResponse");
    proto = protoGenerator.convert();
    assertEquals("""
        syntax = "proto3";
        package test.int;
                
        //@WrapProperty
        message testIntResponse {
          int32 value = 1;
        }
        """.trim(), new ProtoToStringGenerator(proto).protoToString().trim());
  }


  @Test
  public void test_springmvc_model_schema_correct() {
    SpringmvcSwaggerGenerator generator = new SpringmvcSwaggerGenerator(SpringMvcSchema.class);
    OpenAPI openAPI = generator.generate();

    SchemaToProtoGenerator protoGenerator =
        new SchemaToProtoGenerator("test.model", openAPI,
            openAPI.getPaths().get("/testModel").getPost()
                .getRequestBody().getContent().get(MediaType.APPLICATION_JSON)
                .getSchema(), "testModelRequest");
    Proto proto = protoGenerator.convert();
    assertEquals("""
        syntax = "proto3";
        package test.model;
                
        message Model {
          string name = 1;
          int32 age = 2;
        }
                
        //@WrapProperty
        message testModelRequest {
          Model value = 1;
        }
        """.trim(), new ProtoToStringGenerator(proto).protoToString().trim());

    protoGenerator =
        new SchemaToProtoGenerator("test.model", openAPI,
            openAPI.getPaths().get("/testModel").getPost()
                .getResponses().get("200").getContent().get(MediaType.APPLICATION_JSON)
                .getSchema(), "testIntResponse");
    proto = protoGenerator.convert();
    assertEquals("""
        syntax = "proto3";
        package test.model;
                
        message Model {
          string name = 1;
          int32 age = 2;
        }
                
        //@WrapProperty
        message testIntResponse {
          Model value = 1;
        }
        """.trim(), new ProtoToStringGenerator(proto).protoToString().trim());
  }
}
//CHECKSTYLE:ON
