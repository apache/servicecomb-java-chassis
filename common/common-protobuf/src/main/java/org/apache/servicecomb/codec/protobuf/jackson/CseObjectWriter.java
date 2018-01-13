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

package org.apache.servicecomb.codec.protobuf.jackson;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectWriter;

@SuppressWarnings("unchecked")
public class CseObjectWriter extends ObjectWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CseObjectWriter.class);

  private static final long serialVersionUID = -6435897284942268001L;

  private static Constructor<Prefetch> prefetchConstructor;

  static {
    prefetchConstructor = (Constructor<Prefetch>) Prefetch.class.getDeclaredConstructors()[0];
    prefetchConstructor.setAccessible(true);
  }

  private static Prefetch createPrefetch(JsonSerializer<Object> valueSerializer) {
    try {
      return prefetchConstructor.newInstance(null, valueSerializer, null);
    } catch (Exception e) {
      LOGGER.error("create prefetch error:", e);
    }
    return null;
  }

  public CseObjectWriter(ObjectWriter base, FormatSchema schema, JsonSerializer<Object> valueSerializer) {

    super(base, base.getConfig(), new GeneratorSettings(null, schema, null, null),
        createPrefetch(valueSerializer));
  }
}
