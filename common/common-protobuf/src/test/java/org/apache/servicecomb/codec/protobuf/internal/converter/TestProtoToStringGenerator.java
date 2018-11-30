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

import org.apache.servicecomb.foundation.protobuf.internal.parser.ProtoParser;
import org.junit.Assert;
import org.junit.Test;

import io.protostuff.compiler.model.Proto;

public class TestProtoToStringGenerator {
  static String content = "syntax = \"proto3\";\n"
      + "import \"google/protobuf/any.proto\";\n"
      + "package org.apache.servicecomb.foundation.protobuf.internal.model;\n"
      + "\n"
      + "message Root {\n"
      + "  int32 int32 = 1;\n"
      + "  int64 int64 = 2;\n"
      + "  uint32 uint32 = 3;\n"
      + "  uint64 uint64 = 4;\n"
      + "  sint32 sint32 = 5;\n"
      + "  sint64 sint64 = 6;\n"
      + "  fixed32 fixed32 = 7;\n"
      + "  fixed64 fixed64 = 8;\n"
      + "  sfixed32 sfixed32 = 9;\n"
      + "  sfixed64 sfixed64 = 10;\n"
      + "  float floatValue = 11;\n"
      + "  double doubleValue = 12;\n"
      + "  bool bool = 13;\n"
      + "  string string = 14;\n"
      + "  bytes bytes = 15;\n"
      + "  Color color = 16;\n"
      + "  User user = 17;\n"
      + "  map<string, string> ssMap = 18;\n"
      + "  map<string, User> spMap = 19;\n"
      + "  repeated string sList = 20;\n"
      + "  repeated User pList = 21;\n"
      + "  google.protobuf.Any any = 22;\n"
      + "  repeated google.protobuf.Any anys = 23;\n"
      + "  Root typeRecursive = 24;\n"
      + "}\n"
      + "\n"
      + "message User {\n"
      + "  string name = 1;\n"
      + "  Root typeRecursive = 2;\n"
      + "}\n"
      + "\n"
      + "enum Color {\n"
      + "  RED = 0;\n"
      + "  YELLOW = 1;\n"
      + "  BLUE = 2;\n"
      + "}\n"
      + "\n"
      + "service Service {\n"
      + "  //comment\n"
      + "  rpc op1 (User) returns (Root);\n"
      + "\n"
      + "  rpc op2 (Root) returns (User);\n"
      + "}\n";

  @Test
  public void protoToString() {
    ProtoParser protoParser = new ProtoParser();
    Proto proto = protoParser.parseFromContent(content);
    String newContent = new ProtoToStringGenerator(proto).protoToString();

    Assert.assertEquals(content, newContent);
  }
}
