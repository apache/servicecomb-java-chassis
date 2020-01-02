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
package org.apache.servicecomb.foundation.protobuf;

import java.io.IOException;

import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyWrapper;

public class ResponseRootDeserializer<T> {
  private boolean wrapProperty;

  private RootDeserializer<T> rootDeserializer;

  private boolean empty;

  public ResponseRootDeserializer(RootDeserializer<T> rootDeserializer, boolean wrapProperty, boolean empty) {
    this.rootDeserializer = rootDeserializer;
    this.wrapProperty = wrapProperty;
    this.empty = empty;
  }

  @SuppressWarnings("unchecked")
  public T deserialize(byte[] bytes) throws IOException {
    if (empty) {
      T a = rootDeserializer.deserialize(bytes); // read buffers if possible.
      return null;
    }

    if (wrapProperty) {
      return ((PropertyWrapper<T>) rootDeserializer.deserialize(bytes)).getValue();
    } else {
      return rootDeserializer.deserialize(bytes);
    }
  }
}
