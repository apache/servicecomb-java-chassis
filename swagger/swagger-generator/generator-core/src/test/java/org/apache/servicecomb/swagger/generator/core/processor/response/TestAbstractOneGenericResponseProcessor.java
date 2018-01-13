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
package org.apache.servicecomb.swagger.generator.core.processor.response;

import java.lang.reflect.Method;

import javax.xml.ws.Holder;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.ResponseTypeProcessor;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import mockit.Expectations;
import mockit.Mocked;

public class TestAbstractOneGenericResponseProcessor {
  static ResponseTypeProcessor processor = new AbstractOneGenericResponseProcessor() {
    @SuppressWarnings("unused")
    public Holder<String> generic() {
      return null;
    }

    @Override
    public Class<?> getResponseType() {
      return null;
    }
  };

  @Test
  public void process(@Mocked OperationGenerator operationGenerator) {
    Method providerMethod = ReflectUtils.findMethod(processor.getClass(), "generic");
    new Expectations() {
      {
        operationGenerator.getProviderMethod();
        result = providerMethod;
      }
    };
    Property property = processor.process(operationGenerator);

    Assert.assertThat(property, Matchers.instanceOf(StringProperty.class));
  }
}
