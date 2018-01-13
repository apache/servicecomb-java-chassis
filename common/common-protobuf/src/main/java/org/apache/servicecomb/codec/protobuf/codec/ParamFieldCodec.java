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

package org.apache.servicecomb.codec.protobuf.codec;

import java.lang.reflect.Type;

import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.codec.protobuf.jackson.CseObjectReader;
import org.apache.servicecomb.codec.protobuf.jackson.CseObjectWriter;
import org.apache.servicecomb.codec.protobuf.jackson.ParamDeserializer;
import org.apache.servicecomb.codec.protobuf.jackson.ParamSerializer;

import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;

public class ParamFieldCodec extends AbstractFieldCodec {
  @Override
  public void init(ProtobufSchema schema, Type... types) {
    writer = new CseObjectWriter(ProtobufManager.getWriter(), schema, new ParamSerializer());
    reader =
        new CseObjectReader(ProtobufManager.getReader(), schema, new ParamDeserializer(readerHelpDataMap));

    super.init(schema, types);
  }
}
