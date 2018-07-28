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

package org.apache.servicecomb.foundation.common.utils.json;

import java.io.IOException;

import javax.servlet.http.Part;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class JavaxServletPartSerializer extends StdSerializer<Part> {
  private static final long serialVersionUID = 348443113789878443L;

  public JavaxServletPartSerializer() {
    this(null);
  }

  protected JavaxServletPartSerializer(Class<Part> t) {
    super(t);
  }

  @Override
  public void serialize(Part value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    final ObjectCodec preservedCodec = ((TokenBuffer) gen).asParser().getCodec();
    // set codec as null to avoid recursive dead loop
    // JsonGenerator is instantiated for each serialization, so there should be no thread safe issue
    gen.setCodec(null);
    gen.writeObject(value);
    gen.setCodec(preservedCodec);
  }
}
