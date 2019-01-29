//========================================================================
//Copyright 2007-2010 David Yu dyuproject@gmail.com
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

package io.protostuff.runtime;

import java.util.Collection;
import java.util.List;

/**
 * Forked and modified from protostuff
 *
 * Interface for map of fields - defines how to you get field by name or number (tag).
 *
 * @author Kostiantyn Shchepanovskyi
 */
public interface FieldMapEx<T> {
  int MIN_TAG_FOR_HASH_FIELD_MAP = 100;

  static boolean preferHashFieldMap(int fieldCount, int lastFieldNumber) {
    return lastFieldNumber > MIN_TAG_FOR_HASH_FIELD_MAP && lastFieldNumber >= 2 * fieldCount;
  }

  static <T> FieldMapEx<T> createFieldMap(Collection<FieldSchema<T>> fields) {
    int lastFieldNumber = 0;
    for (FieldSchema<T> field : fields) {
      if (field.getFieldNumber() > lastFieldNumber) {
        lastFieldNumber = field.getFieldNumber();
      }
    }
    if (preferHashFieldMap(fields.size(), lastFieldNumber)) {
      return new HashFieldMapEx<>(fields);
    }
    // array field map should be more efficient
    return new ArrayFieldMapEx<>(fields, lastFieldNumber);
  }

  FieldSchema<T> getFieldByNumber(int n);

  FieldSchema<T> getFieldByName(String fieldName);

  int getFieldCount();

  List<FieldSchema<T>> getFields();
}
