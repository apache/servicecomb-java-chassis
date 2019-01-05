//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package io.protostuff;

/**
 * Forked and modified from protostuff
 *
 * Handles the serialization and deserialization of a message/object tied to this.
 * <p>
 * Basically, any object can be serialized via protobuf. As long as its schema is provided, it does not need to
 * implement {@link Message}. This was designed with "unobtrusive" in mind. The goal was to be able to
 * serialize/deserialize any existing object without having to touch its source. This will enable you to customize the
 * serialization of objects from 3rd party libraries.
 *
 * @author David Yu
 * @created Nov 9, 2009
 */
public interface SchemaEx<T> extends SchemaWriter<T>, SchemaReader<T> {
  /**
   * Returns the simple name of the message tied to this schema. Allows custom schemas to provide a custom name other
   * than typeClass().getSimpleName();
   */
  default String messageName() {
    throw new UnsupportedOperationException();
  }

  void init();
}
