/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.servicecomb.foundation.common.utils.BeanUtils;
import mockit.Mock;
import mockit.MockUp;

public class MockUtil {

  private static MockUtil instance = new MockUtil();

  private MockUtil() {

  }

  public static MockUtil getInstance() {
    return instance;
  }

  public void mockBeanUtils() {

    new MockUp<BeanUtils>() {
      @Mock
      ApplicationContext getContext() {
        return Mockito.mock(ApplicationContext.class);
      }
    };
  }

  public void mockMethod() {

    new MockUp<Method>() {
      @Mock
      public Annotation[][] getParameterAnnotations() {
        Annotation[][] lAnnotation = new Annotation[1][1];
        lAnnotation[0][0] = Mockito.mock(Annotation.class);
        return lAnnotation;
      }

      @Mock
      public Type[] getGenericParameterTypes() {
        Type[] lType = new Type[1];
        lType[0] = Mockito.mock(Type.class);
        Mockito.when(lType[0].getTypeName()).thenReturn("javax.servlet.http.HttpServletRequest");
        return lType;
      }
    };
  }
}
