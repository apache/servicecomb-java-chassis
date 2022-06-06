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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.servicecomb.foundation.protobuf.internal.parser.ProtoParser;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.Proto;

public final class ProtoConst {
  private ProtoConst() {
  }

  public static final String ANNOTATION_WRAP_ARGUMENTS = "@WrapArguments";

  public static final String ANNOTATION_WRAP_PROPERTY = "@WrapProperty";

  public static String ANNOTATION_RPC = "@Rpc";

  public static final String PACK_SCHEMA = "type.googleapis.com/";

  public static final String JSON_SCHEMA = "json/";

  public static final String JSON_ID_NAME = "@type";

  public static final JavaType MAP_TYPE = TypeFactory.defaultInstance().constructType(LinkedHashMap.class);

  public static final JavaType LIST_TYPE = TypeFactory.defaultInstance().constructType(ArrayList.class);

  public static final JavaType OBJECT_TYPE = TypeFactory.defaultInstance().constructType(Object.class);

  public static final Proto ANY_PROTO;

  public static final Message ANY;

  public static final Proto EMPTY_PROTO;

  public static final Message EMPTY;

  static {
    ProtoParser protoParser = new ProtoParser();

    ANY_PROTO = protoParser.parse("google/protobuf/any.proto");
    ANY = ANY_PROTO.getMessage("Any");

    EMPTY_PROTO = protoParser.parse("google/protobuf/empty.proto");
    EMPTY = EMPTY_PROTO.getMessage("Empty");
  }
}
