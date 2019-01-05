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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Forked and modified from protostuff
 *
 * Field mapping implemented on top of hash for field lookup by number.
 *
 * This is the less efficient than {@code ArrayFieldMapEx} for almost all cases.
 * But in case when field numbers are sparse and especially when max field
 * number is big - this mapping should be used.
 *
 * @see ArrayFieldMapEx
 *
 * @author Kostiantyn Shchepanovskyi
 */
public final class HashFieldMapEx<T> implements FieldMapEx<T> {
  private final List<FieldSchema<T>> fields;

  private final Map<Integer, FieldSchema<T>> fieldsByNumber;

  private final Map<String, FieldSchema<T>> fieldsByName;

  public HashFieldMapEx(Collection<FieldSchema<T>> fields) {
    fieldsByName = new HashMap<>();
    fieldsByNumber = new HashMap<>();
    for (FieldSchema<T> f : fields) {
      if (fieldsByName.containsKey(f.name)) {
        FieldSchema<T> prev = fieldsByName.get(f.name);
        throw new IllegalStateException(prev + " and " + f + " cannot have the same name.");
      }
      if (fieldsByNumber.containsKey(f.fieldNumber)) {
        FieldSchema<T> prev = fieldsByNumber.get(f.fieldNumber);
        throw new IllegalStateException(prev + " and " + f + " cannot have the same number.");
      }
      this.fieldsByNumber.put(f.fieldNumber, f);
      this.fieldsByName.put(f.name, f);
    }

    List<FieldSchema<T>> fieldList = new ArrayList<>(fields.size());
    fieldList.addAll(fields);
    Collections.sort(fieldList, Comparator.comparingInt(FieldSchema::getFieldNumber));
    this.fields = Collections.unmodifiableList(fieldList);
  }

  @Override
  public FieldSchema<T> getFieldByNumber(int n) {
    return fieldsByNumber.get(n);
  }

  @Override
  public FieldSchema<T> getFieldByName(String fieldName) {
    return fieldsByName.get(fieldName);
  }

  @Override
  public int getFieldCount() {
    return fields.size();
  }

  @Override
  public List<FieldSchema<T>> getFields() {
    return fields;
  }
}
