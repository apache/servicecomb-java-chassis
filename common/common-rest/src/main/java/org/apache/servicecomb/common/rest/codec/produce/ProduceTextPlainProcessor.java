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

package org.apache.servicecomb.common.rest.codec.produce;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JavaType;

import jakarta.ws.rs.core.MediaType;

public class ProduceTextPlainProcessor extends ProduceJsonProcessor {
  @Override
  public String getName() {
    return MediaType.TEXT_PLAIN;
  }

  @Override
  public void doEncodeResponse(OutputStream output, Object result) throws Exception {
    if (result instanceof String) {
      output.write(((String) result).getBytes(StandardCharsets.UTF_8));
      return;
    }
    super.doEncodeResponse(output, result);
  }

  @Override
  public Object doDecodeResponse(InputStream input, JavaType type) throws Exception {
    if (String.class.equals(type.getRawClass())) {
      return IOUtils.toString(input, StandardCharsets.UTF_8);
    }
    return super.doDecodeResponse(input, type);
  }
}
