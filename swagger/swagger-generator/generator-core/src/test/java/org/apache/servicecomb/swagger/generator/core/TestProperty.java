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

package org.apache.servicecomb.swagger.generator.core;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.swagger.converter.SwaggerToClassGenerator;
import org.apache.servicecomb.swagger.converter.property.StringPropertyConverter;
import org.apache.servicecomb.swagger.generator.core.unittest.SwaggerGeneratorForTest;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.converter.ModelConverters;
import io.swagger.models.properties.StringProperty;

public class TestProperty {
  SwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();

  @Test
  public void testStringProperty() {
    SwaggerGenerator generator = new SwaggerGeneratorForTest(context, null);

    List<String> enums = Arrays.asList("testStringProperty_a", "testStringProperty_b");

    StringProperty sp = new StringProperty();
    sp._enum(enums);

    StringPropertyConverter spc = new StringPropertyConverter();
    JavaType jt = spc.convert(
        new SwaggerToClassGenerator(generator.getClassLoader(), generator.getSwagger(),
            generator.ensureGetPackageName()),
        sp);

    StringProperty spNew = (StringProperty) ModelConverters.getInstance().readAsProperty(jt);
    Assert.assertEquals(enums, spNew.getEnum());
  }
}
