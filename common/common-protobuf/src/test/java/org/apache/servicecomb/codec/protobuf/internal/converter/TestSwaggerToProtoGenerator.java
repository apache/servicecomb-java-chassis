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
package org.apache.servicecomb.codec.protobuf.internal.converter;

import org.apache.servicecomb.codec.protobuf.internal.converter.model.ProtoSchema;
import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import io.protostuff.compiler.model.Proto;
import io.swagger.models.Swagger;
import io.vertx.core.json.Json;

public class TestSwaggerToProtoGenerator {
  static String protoContent = "syntax = \"proto3\";\n"
      + "import \"google/protobuf/empty.proto\";\n"
      + "import \"google/protobuf/any.proto\";\n"
      + "package a.b;\n"
      + "\n"
      + "message Empty {\n"
      + "}\n"
      + "\n"
      + "message User {\n"
      + "  string name = 1;\n"
      + "  repeated User friends = 2;\n"
      + "}\n"
      + "\n"
      + "message Ref1 {\n"
      + "  Ref2 ref = 1;\n"
      + "}\n"
      + "\n"
      + "message Ref2 {\n"
      + "  Ref1 ref = 1;\n"
      + "}\n"
      + "\n"
      + "message baseRequestWrap {\n"
      + "  bool boolValue = 1;\n"
      + "  int32 iValue = 2;\n"
      + "  int64 lValue = 3;\n"
      + "  float fValue = 4;\n"
      + "  double dValue = 5;\n"
      + "  string sValue = 6;\n"
      + "  repeated int32 iArray = 7;\n"
      + "  Enum_2610aa5dc6cd086cf20168892802c9c765a557f4951557340ad9f0982c53e055 color = 8;\n"
      + "  int64 localDate = 9;\n"
      + "  int64 date = 10;\n"
      + "  Empty empty = 11;\n"
      + "}\n"
      + "\n"
      + "message baseResponseWrap444 {\n"
      + "  Enum_2610aa5dc6cd086cf20168892802c9c765a557f4951557340ad9f0982c53e055 response = 1;\n"
      + "}\n"
      + "\n"
      + "message baseResponseWrap200 {\n"
      + "  int32 response = 1;\n"
      + "}\n"
      + "\n"
      + "message bytesRequestWrap {\n"
      + "  bytes value = 1;\n"
      + "}\n"
      + "\n"
      + "message bytesResponseWrap200 {\n"
      + "  bytes response = 1;\n"
      + "}\n"
      + "\n"
      + "message colorBodyRequestWrap {\n"
      + "  Enum_2610aa5dc6cd086cf20168892802c9c765a557f4951557340ad9f0982c53e055 color = 1;\n"
      + "}\n"
      + "\n"
      + "message colorBodyResponseWrap200 {\n"
      + "  Enum_2610aa5dc6cd086cf20168892802c9c765a557f4951557340ad9f0982c53e055 response = 1;\n"
      + "}\n"
      + "\n"
      + "message listObjRequestWrap {\n"
      + "  repeated google.protobuf.Any objs = 1;\n"
      + "}\n"
      + "\n"
      + "message listObjResponseWrap200 {\n"
      + "  repeated google.protobuf.Any response = 1;\n"
      + "}\n"
      + "\n"
      + "message listUserRequestWrap {\n"
      + "  repeated User users = 1;\n"
      + "}\n"
      + "\n"
      + "message listUserResponseWrap200 {\n"
      + "  repeated User response = 1;\n"
      + "}\n"
      + "\n"
      + "message mapObjRequestWrap {\n"
      + "  map<string, google.protobuf.Any> objs = 1;\n"
      + "}\n"
      + "\n"
      + "message mapObjResponseWrap200 {\n"
      + "  map<string, google.protobuf.Any> response = 1;\n"
      + "}\n"
      + "\n"
      + "message mapUserRequestWrap {\n"
      + "  map<string, User> users = 1;\n"
      + "}\n"
      + "\n"
      + "message mapUserResponseWrap200 {\n"
      + "  map<string, User> response = 1;\n"
      + "}\n"
      + "\n"
      + "message userWrapInProtobufRequestWrap {\n"
      + "  User user = 1;\n"
      + "  int32 ivalue = 2;\n"
      + "}\n"
      + "\n"
      + "enum Enum_2610aa5dc6cd086cf20168892802c9c765a557f4951557340ad9f0982c53e055 {\n"
      + "  RED = 0;\n"
      + "  YELLOW = 1;\n"
      + "  BLUE = 2;\n"
      + "}\n"
      + "\n"
      + "service MainService {\n"
      + "  //scb:{\"argTypeName\":\"baseRequestWrap\",\"argWrapped\":true,\"responses\":{\"200\":{\"typeName\":\"baseResponseWrap200\",\"wrapped\":true},\"444\":{\"typeName\":\"baseResponseWrap444\",\"wrapped\":true}}}\n"
      + "  rpc base (baseRequestWrap) returns (baseResponseWrap200);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"bytesRequestWrap\",\"argWrapped\":true,\"responses\":{\"200\":{\"typeName\":\"bytesResponseWrap200\",\"wrapped\":true}}}\n"
      + "  rpc bytes (bytesRequestWrap) returns (bytesResponseWrap200);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"colorBodyRequestWrap\",\"argWrapped\":true,\"responses\":{\"200\":{\"typeName\":\"colorBodyResponseWrap200\",\"wrapped\":true}}}\n"
      + "  rpc colorBody (colorBodyRequestWrap) returns (colorBodyResponseWrap200);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"listObjRequestWrap\",\"argWrapped\":true,\"responses\":{\"200\":{\"typeName\":\"listObjResponseWrap200\",\"wrapped\":true}}}\n"
      + "  rpc listObj (listObjRequestWrap) returns (listObjResponseWrap200);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"listUserRequestWrap\",\"argWrapped\":true,\"responses\":{\"200\":{\"typeName\":\"listUserResponseWrap200\",\"wrapped\":true}}}\n"
      + "  rpc listUser (listUserRequestWrap) returns (listUserResponseWrap200);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"mapObjRequestWrap\",\"argWrapped\":true,\"responses\":{\"200\":{\"typeName\":\"mapObjResponseWrap200\",\"wrapped\":true}}}\n"
      + "  rpc mapObj (mapObjRequestWrap) returns (mapObjResponseWrap200);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"mapUserRequestWrap\",\"argWrapped\":true,\"responses\":{\"200\":{\"typeName\":\"mapUserResponseWrap200\",\"wrapped\":true}}}\n"
      + "  rpc mapUser (mapUserRequestWrap) returns (mapUserResponseWrap200);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"google.protobuf.Empty\",\"argWrapped\":false,\"responses\":{\"200\":{\"typeName\":\"google.protobuf.Empty\",\"wrapped\":false}}}\n"
      + "  rpc noParamVoid (google.protobuf.Empty) returns (google.protobuf.Empty);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"google.protobuf.Any\",\"argWrapped\":false,\"responses\":{\"200\":{\"typeName\":\"google.protobuf.Any\",\"wrapped\":false}}}\n"
      + "  rpc obj (google.protobuf.Any) returns (google.protobuf.Any);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"Ref1\",\"argWrapped\":false,\"responses\":{\"200\":{\"typeName\":\"Ref2\",\"wrapped\":false}}}\n"
      + "  rpc ref (Ref1) returns (Ref2);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"User\",\"argWrapped\":false,\"responses\":{\"200\":{\"typeName\":\"User\",\"wrapped\":false}}}\n"
      + "  rpc user (User) returns (User);\n"
      + "\n"
      + "  //scb:{\"argTypeName\":\"userWrapInProtobufRequestWrap\",\"argWrapped\":true,\"responses\":{\"200\":{\"typeName\":\"User\",\"wrapped\":false}}}\n"
      + "  rpc userWrapInProtobuf (userWrapInProtobufRequestWrap) returns (User);\n"
      + "}\n";

  @Test
  public void convert() {
    SwaggerGeneratorContext context = new SpringmvcSwaggerGeneratorContext();
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(context, ProtoSchema.class);
    Swagger swagger = swaggerGenerator.generate();

    SwaggerToProtoGenerator generator = new SwaggerToProtoGenerator("a.b", swagger);
    Proto proto = generator.convert();

    Assert.assertEquals(protoContent, new ProtoToStringGenerator(proto).protoToString());
  }

  public static void main(String[] args) {
    String json = Json.encode(Color.BLUE);
    System.out.println(json);
    System.out.println(Json.decodeValue(json, Color.class));
    System.out.println(Json.decodeValue("2", Color.class));
    System.out.println(Json.mapper.convertValue("BLUE", Color.class));
  }
}
