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

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;

import com.fasterxml.jackson.databind.JavaType;

public class ProduceJsonProcessor implements ProduceProcessor {

  private Class<?> serializationView;

  @Override
  public String getSerializationView() {
    return serializationView == null ? ProduceProcessor.super.getSerializationView()
        : serializationView.getName();
  }

  @Override
  public void setSerializationView(Class<?> serializationView) {
    if (serializationView == null) {
      return;
    }
    this.serializationView = serializationView;
  }

  @Override
  public String getName() {
    return MediaType.APPLICATION_JSON;
  }

  @Override
  public void doEncodeResponse(OutputStream output, Object result) throws Exception {
    if (serializationView == null) {
      RestObjectMapperFactory.getRestObjectMapper().writeValue(output, result);
      return;
    }
    RestObjectMapperFactory.getRestObjectMapper().writerWithView(serializationView).writeValue(output, result);
  }

  @Override
  public Object doDecodeResponse(InputStream input, JavaType type) throws Exception {
    if (serializationView == null) {
      return RestObjectMapperFactory.getRestObjectMapper().readValue(input, type);
    }
    return RestObjectMapperFactory.getRestObjectMapper().readerWithView(serializationView)
        .forType(type).readValue(input);
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
