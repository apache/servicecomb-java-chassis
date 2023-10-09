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
package org.apache.servicecomb.common.rest.codec.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;

public abstract class AbstractQueryCodec implements QueryCodec {
  private final String codecName;

  public AbstractQueryCodec(String codecName) {
    this.codecName = codecName;
  }

  @Override
  public String getCodecName() {
    return codecName;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void encode(URLPathStringBuilder builder, String name, Object value) throws Exception {
    if (value == null) {
      // not write query key to express "null"
      return;
    }

    if (value.getClass().isArray()) {
      encode(builder, name, Arrays.asList((Object[]) value));
      return;
    }

    if (value instanceof Collection) {
      encode(builder, name, (Collection<Object>) value);
      return;
    }

    encode(builder, name, Collections.singletonList(value));
  }

  abstract void encode(URLPathStringBuilder builder, String name, Collection<Object> values) throws Exception;
}
