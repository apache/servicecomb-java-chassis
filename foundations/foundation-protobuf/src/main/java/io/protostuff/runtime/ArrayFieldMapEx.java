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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Forked and modified from protostuff
 *
 * Field mapping implemented on top of java array for lookup by number.
 *
 * This is the most efficient implementation for almost all cases. But
 * it should not be used when field numbers are sparse and especially
 * when max field number is big - as this mapping internally uses array
 * of integers with size equal to max field number. In latter case
 * {@code HashFieldMapEx} should be used.
 *
 * @see HashFieldMapEx
 *
 * @author Kostiantyn Shchepanovskyi
 */
public final class ArrayFieldMapEx<T> implements FieldMapEx<T> {
  private final List<FieldSchema<T>> fields;

  private final FieldSchema<T>[] fieldsByNumber;

  private final Map<String, FieldSchema<T>> fieldsByName;

  @SuppressWarnings("unchecked")
  public ArrayFieldMapEx(Collection<FieldSchema<T>> fields, int lastFieldNumber) {
    fieldsByName = new HashMap<>();
    fieldsByNumber = (FieldSchema<T>[]) new FieldSchema<?>[lastFieldNumber + 1];
    for (FieldSchema<T> f : fields) {
      FieldSchema<T> last = this.fieldsByName.put(f.name, f);
      if (last != null) {
        throw new IllegalStateException(last + " and " + f + " cannot have the same name.");
      }
      if (fieldsByNumber[f.getFieldNumber()] != null) {
        throw new IllegalStateException(
            fieldsByNumber[f.getFieldNumber()] + " and " + f + " cannot have the same number.");
      }

      fieldsByNumber[f.getFieldNumber()] = f;
    }

    List<FieldSchema<T>> fieldList = new ArrayList<>(fields.size());
    for (FieldSchema<T> field : fieldsByNumber) {
      if (field != null) {
        fieldList.add(field);
      }
    }
    this.fields = Collections.unmodifiableList(fieldList);
  }

  @Override
  public FieldSchema<T> getFieldByNumber(int n) {
    return n < fieldsByNumber.length ? fieldsByNumber[n] : null;
  }

  @Override
  public FieldSchema<T> getFieldByName(String fieldName) {
    return fieldsByName.get(fieldName);
  }

  /**
   * Returns the message's total number of fields.
   */
  @Override
  public int getFieldCount() {
    return fields.size();
  }

  @Override
  public List<FieldSchema<T>> getFields() {
    return fields;
  }
}
