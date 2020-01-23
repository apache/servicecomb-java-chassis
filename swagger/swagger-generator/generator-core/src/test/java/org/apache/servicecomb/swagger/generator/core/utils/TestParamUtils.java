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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.servicecomb.swagger.generator.core.utils.paramUtilsModel.AbstractBaseService;
import org.apache.servicecomb.swagger.generator.core.utils.paramUtilsModel.AbstractBean;
import org.apache.servicecomb.swagger.generator.core.utils.paramUtilsModel.IBaseService;
import org.apache.servicecomb.swagger.generator.core.utils.paramUtilsModel.IMyService;
import org.apache.servicecomb.swagger.generator.core.utils.paramUtilsModel.MyEndpoint;
import org.apache.servicecomb.swagger.generator.core.utils.paramUtilsModel.MyEndpoint2;
import org.apache.servicecomb.swagger.generator.core.utils.paramUtilsModel.PersonBean;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

public class TestParamUtils {

  @Test
  public void testGenericTypeInheritance() throws Exception {
    Method hello = IMyService.class.getMethod("hello", AbstractBean.class);
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(IMyService.class, IBaseService.class, hello.getGenericReturnType()));
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(IMyService.class, hello, hello.getParameters()[0]));

    hello = MyEndpoint.class.getMethod("hello", AbstractBean.class);
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(MyEndpoint.class, AbstractBaseService.class, hello.getGenericReturnType()));
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(MyEndpoint.class, hello, hello.getParameters()[0]));

    hello = MyEndpoint2.class.getMethod("hello", PersonBean.class);
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(MyEndpoint2.class, MyEndpoint2.class, hello.getGenericReturnType()));
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(MyEndpoint2.class, hello, hello.getParameters()[0]));

    Method helloBody = IMyService.class.getMethod("helloBody", AbstractBean[].class);
    assertEquals(PersonBean[].class,
        ParamUtils.getGenericParameterType(IMyService.class, IBaseService.class, helloBody.getGenericReturnType()));
    assertEquals(PersonBean[].class,
        ParamUtils.getGenericParameterType(MyEndpoint.class, helloBody, helloBody.getParameters()[0]));

    helloBody = MyEndpoint.class.getMethod("helloBody", AbstractBean[].class);
    assertEquals(PersonBean[].class, ParamUtils
        .getGenericParameterType(MyEndpoint.class, AbstractBaseService.class, helloBody.getGenericReturnType()));
    assertEquals(PersonBean[].class,
        ParamUtils.getGenericParameterType(MyEndpoint.class, helloBody, helloBody.getParameters()[0]));

    Method helloList = IMyService.class.getMethod("helloList", List.class);
    assertEquals(TypeUtils.parameterize(List.class, PersonBean.class),
        ParamUtils.getGenericParameterType(IMyService.class, IBaseService.class, helloList.getGenericReturnType()));
    assertEquals(TypeUtils.parameterize(List.class, PersonBean.class),
        ParamUtils.getGenericParameterType(IMyService.class, helloList, helloList.getParameters()[0]));

    helloList = MyEndpoint.class.getMethod("helloList", List.class);
    assertEquals(TypeUtils.parameterize(List.class, PersonBean.class), ParamUtils
        .getGenericParameterType(MyEndpoint.class, AbstractBaseService.class, helloList.getGenericReturnType()));
    assertEquals(TypeUtils.parameterize(List.class, PersonBean.class),
        ParamUtils.getGenericParameterType(MyEndpoint.class, helloList, helloList.getParameters()[0]));

    Method actual = IMyService.class.getMethod("actual", PersonBean.class);
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(IMyService.class, IBaseService.class, actual.getGenericReturnType()));
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(IMyService.class, actual, actual.getParameters()[0]));

    helloList = MyEndpoint.class.getMethod("actual", PersonBean.class);
    assertEquals(PersonBean.class,
        ParamUtils
            .getGenericParameterType(MyEndpoint.class, AbstractBaseService.class, helloList.getGenericReturnType()));
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(MyEndpoint.class, helloList, helloList.getParameters()[0]));

    Method parentHello = IMyService.class.getMethod("parentHello", List.class);
    assertEquals(TypeUtils.parameterize(List.class, MultipartFile.class),
        ParamUtils
            .getGenericParameterType(IMyService.class, IMyService.class, parentHello.getGenericReturnType()));
    assertEquals(TypeUtils.parameterize(List.class, MultipartFile.class),
        ParamUtils
            .getGenericParameterType(IMyService.class, parentHello, parentHello.getParameters()[0]));
  }

  @Test
  public void testGenericTypeInheritanceWithMethodUtils() throws Exception {
    List<Method> methods = MethodUtils.findSwaggerMethods(MyEndpoint.class);
    Assert.assertEquals(5, methods.size());
    assertEquals(PersonBean.class, ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(0).getDeclaringClass(),
            methods.get(0).getGenericReturnType())); // actual
    assertEquals(PersonBean.class, ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(0),
            methods.get(0).getParameters()[0])); // actual
    assertEquals(PersonBean.class, ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(1).getDeclaringClass(),
            methods.get(1).getGenericReturnType())); // hello
    assertEquals(PersonBean.class, ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(1),
            methods.get(1).getParameters()[0])); // hello
    assertEquals(PersonBean[].class, ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(2).getDeclaringClass(),
            methods.get(2).getGenericReturnType())); // helloBody
    assertEquals(PersonBean[].class, ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(2),
            methods.get(2).getParameters()[0])); // helloBody
    assertEquals(TypeUtils.parameterize(List.class, PersonBean.class), ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(3).getDeclaringClass(),
            methods.get(3).getGenericReturnType())); // helloList
    assertEquals(TypeUtils.parameterize(List.class, PersonBean.class), ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(3),
            methods.get(3).getParameters()[0])); // helloList
    assertEquals(TypeUtils.parameterize(List.class, MultipartFile.class), ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(4).getDeclaringClass(),
            methods.get(4).getGenericReturnType())); // parentHello
    assertEquals(TypeUtils.parameterize(List.class, MultipartFile.class), ParamUtils
        .getGenericParameterType(MyEndpoint.class, methods.get(4),
            methods.get(4).getParameters()[0])); // parentHello
  }
}
