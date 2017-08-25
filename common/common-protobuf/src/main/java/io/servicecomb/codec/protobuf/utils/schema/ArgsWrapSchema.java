/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.codec.protobuf.utils.schema;

import java.io.IOException;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Schema;
import io.servicecomb.common.javassist.MultiWrapper;

public class ArgsWrapSchema extends AbstractWrapSchema {

  @SuppressWarnings("unchecked")
  public ArgsWrapSchema(Schema<?> schema) {
    this.schema = (Schema<Object>) schema;
  }

  @Override
  public Object readFromEmpty() {
    MultiWrapper wrapper = (MultiWrapper) schema.newMessage();
    return wrapper.readFields();
  }

  public Object readObject(Input input) throws IOException {
    MultiWrapper wrapper = (MultiWrapper) schema.newMessage();
    schema.mergeFrom(input, wrapper);

    return wrapper.readFields();
  }

  public void writeObject(Output output, Object value) throws IOException {
    MultiWrapper wrapper = (MultiWrapper) schema.newMessage();
    wrapper.writeFields((Object[]) value);

    schema.writeTo(output, wrapper);
  }
}
