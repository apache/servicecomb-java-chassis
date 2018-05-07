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
package org.apache.servicecomb.swagger.converter;

import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.servicecomb.swagger.extend.property.ByteProperty;
import org.apache.servicecomb.swagger.extend.property.ShortProperty;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

public class TestConverterMgr {
  @Test
  public void propertyMapGenericSignature() throws IllegalAccessException {
    @SuppressWarnings("unchecked")
    Map<Class<? extends Property>, JavaType> propertyMap = (Map<Class<? extends Property>, JavaType>) FieldUtils
        .readStaticField(ConverterMgr.class, "PROPERTY_MAP", true);

    Assert.assertEquals("Ljava/lang/Boolean;", propertyMap.get(BooleanProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/lang/Float;", propertyMap.get(FloatProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/lang/Double;", propertyMap.get(DoubleProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/math/BigDecimal;", propertyMap.get(DecimalProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/lang/Byte;", propertyMap.get(ByteProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/lang/Short;", propertyMap.get(ShortProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/lang/Integer;", propertyMap.get(IntegerProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/lang/Integer;", propertyMap.get(BaseIntegerProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/lang/Long;", propertyMap.get(LongProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/lang/String;", propertyMap.get(StringProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/time/LocalDate;", propertyMap.get(DateProperty.class).getGenericSignature());
    Assert.assertEquals("Ljava/util/Date;", propertyMap.get(DateTimeProperty.class).getGenericSignature());
    Assert.assertEquals("[B;", propertyMap.get(ByteArrayProperty.class).getGenericSignature());
    Assert.assertEquals("Ljavax/servlet/http/Part;", propertyMap.get(FileProperty.class).getGenericSignature());
  }
}
