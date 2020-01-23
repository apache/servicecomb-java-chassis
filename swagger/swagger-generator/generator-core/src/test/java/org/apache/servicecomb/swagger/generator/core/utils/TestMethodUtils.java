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

package org.apache.servicecomb.swagger.generator.core.utils;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.servicecomb.swagger.generator.core.utils.methodUtilsModel.AbstractBaseClass;
import org.apache.servicecomb.swagger.generator.core.utils.methodUtilsModel.BaseInterface;
import org.apache.servicecomb.swagger.generator.core.utils.methodUtilsModel.Hello2Endpoint;
import org.apache.servicecomb.swagger.generator.core.utils.methodUtilsModel.HelloEndpoint;
import org.apache.servicecomb.swagger.generator.core.utils.methodUtilsModel.ServiceInterface;
import org.junit.Assert;
import org.junit.Test;

public class TestMethodUtils {
  @Test
  public void testGetClassMethods() throws Exception {
    List<Method> methods = MethodUtils.findSwaggerMethods(Hello2Endpoint.class);
    Assert.assertEquals(3, methods.size());
    Assert.assertEquals(Hello2Endpoint.class, methods.get(0).getDeclaringClass());
    Assert.assertEquals(Hello2Endpoint.class, methods.get(1).getDeclaringClass());
    Assert.assertEquals(Hello2Endpoint.class, methods.get(2).getDeclaringClass());

    methods = MethodUtils.findSwaggerMethods(HelloEndpoint.class);
    Assert.assertEquals(2, methods.size());
    Assert.assertEquals(HelloEndpoint.class, methods.get(0).getDeclaringClass()); // get
    Assert.assertEquals(AbstractBaseClass.class, methods.get(1).getDeclaringClass()); // getBase

    methods = MethodUtils.findSwaggerMethods(ServiceInterface.class);
    Assert.assertEquals(3, methods.size());
    Assert.assertEquals(BaseInterface.class, methods.get(0).getDeclaringClass()); // get
    Assert.assertEquals(BaseInterface.class, methods.get(1).getDeclaringClass()); // getArray
    Assert.assertEquals(ServiceInterface.class, methods.get(2).getDeclaringClass()); // getBase
  }
}
