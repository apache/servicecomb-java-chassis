/*
 * Copyright 2016 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Forked from https://github.com/swagger-api/swagger-core/blob/master/modules/swagger-models/src/main/java/io/swagger/models/properties/IntegerProperty.java
 */

package org.apache.servicecomb.swagger.extend.property;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.Xml;
import io.swagger.models.properties.BaseIntegerProperty;

public abstract class AbstractBaseIntegerProperty<T> extends BaseIntegerProperty {
  protected T defaultValue;

  protected List<T> enumNames;

  public AbstractBaseIntegerProperty(String format) {
    super(format);
  }

  public AbstractBaseIntegerProperty<T> addEnum(T value) {
    if (this.enumNames == null) {
      this.enumNames = new ArrayList<>();
    }
    if (!enumNames.contains(value)) {
      enumNames.add(value);
    }
    return this;
  }

  public AbstractBaseIntegerProperty<T> replaceEnum(List<T> value) {
    this.enumNames = value;
    return this;
  }

  public AbstractBaseIntegerProperty<T> xml(Xml xml) {
    this.setXml(xml);
    return this;
  }

  public AbstractBaseIntegerProperty<T> readOnly() {
    this.setReadOnly(Boolean.TRUE);
    return this;
  }

  protected abstract T parseNumber(String strValue);

  public AbstractBaseIntegerProperty<T> assignDefault(String defaultValue) {
    if (defaultValue != null) {
      try {
        this.defaultValue = parseNumber(defaultValue);
      } catch (NumberFormatException e) {
        // continue;
      }
    }
    return this;
  }

  public AbstractBaseIntegerProperty<T> assignDefault(T defaultValue) {
    this.setDefault(defaultValue);
    return this;
  }

  public AbstractBaseIntegerProperty<T> vendorExtension(String key, Object obj) {
    this.setVendorExtension(key, obj);
    return this;
  }

  public T getDefault() {
    return defaultValue;
  }

  public void setDefault(String defaultValue) {
    this.assignDefault(defaultValue);
  }

  public void setDefault(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  public List<T> getEnum() {
    return enumNames;
  }

  public void setEnum(List<T> enums) {
    this.enumNames = enums;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof AbstractBaseIntegerProperty)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    AbstractBaseIntegerProperty<T> other = (AbstractBaseIntegerProperty<T>) obj;
    if (defaultValue == null) {
      if (other.defaultValue != null) {
        return false;
      }
    } else if (!defaultValue.equals(other.defaultValue)) {
      return false;
    }
    return true;
  }
}
