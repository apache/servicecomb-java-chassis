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

package org.apache.servicecomb.transport.rest.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestCseXmlWebApplicationContext {
  @Mocked
  ServletContext servletContext;

  CseXmlWebApplicationContext context;

  @Before
  public void setup() {
    context = new CseXmlWebApplicationContext(servletContext);
  }

  @Test
  public void testGetConfigLocationsEmpty() {
    String[] result = context.getConfigLocations();
    MatcherAssert.assertThat(result,
        Matchers.arrayContaining(BeanUtils.DEFAULT_BEAN_CORE_RESOURCE, BeanUtils.DEFAULT_BEAN_NORMAL_RESOURCE));
  }

  @Test
  public void testGetConfigLocationsEmptyAndDefaultEmpty() {
    context.setDefaultBeanResource(null);
    String[] result = context.getConfigLocations();
    MatcherAssert.assertThat(result,
        Matchers.arrayContaining(BeanUtils.DEFAULT_BEAN_CORE_RESOURCE, BeanUtils.DEFAULT_BEAN_NORMAL_RESOURCE));
  }

  @Test
  public void testGetConfigLocationsComma() {
    new Expectations() {
      {
        servletContext.getInitParameter(CseXmlWebApplicationContext.KEY_LOCATION);
        result = "a,b";
      }
    };

    String[] result = context.getConfigLocations();
    MatcherAssert.assertThat(result, Matchers
        .arrayContaining(BeanUtils.DEFAULT_BEAN_CORE_RESOURCE, BeanUtils.DEFAULT_BEAN_NORMAL_RESOURCE, "a", "b"));
  }

  @Test
  public void testGetConfigLocationsPartEmpty() {
    new Expectations() {
      {
        servletContext.getInitParameter(CseXmlWebApplicationContext.KEY_LOCATION);
        result = "a,,b";
      }
    };
    String[] result = context.getConfigLocations();
    MatcherAssert.assertThat(result, Matchers
        .arrayContaining(BeanUtils.DEFAULT_BEAN_CORE_RESOURCE, BeanUtils.DEFAULT_BEAN_NORMAL_RESOURCE, "a", "b"));
  }

  @Test
  public void testGetConfigLocationsLine() {
    new Expectations() {
      {
        servletContext.getInitParameter(CseXmlWebApplicationContext.KEY_LOCATION);
        result = "a\r\nb";
      }
    };
    String[] result = context.getConfigLocations();
    MatcherAssert.assertThat(result, Matchers
        .arrayContaining(BeanUtils.DEFAULT_BEAN_CORE_RESOURCE, BeanUtils.DEFAULT_BEAN_NORMAL_RESOURCE, "a", "b"));
  }

  @Test
  public void testGetConfigLocationsMix() {
    new Expectations() {
      {
        servletContext.getInitParameter(CseXmlWebApplicationContext.KEY_LOCATION);
        result = "a\r\nb,,c";
      }
    };
    String[] result = context.getConfigLocations();
    MatcherAssert.assertThat(result, Matchers
        .arrayContaining(BeanUtils.DEFAULT_BEAN_CORE_RESOURCE, BeanUtils.DEFAULT_BEAN_NORMAL_RESOURCE, "a", "b", "c"));
  }

  @Test
  public void testInjectServlet(@Mocked ConfigurableListableBeanFactory beanFactory) {
    Holder<Boolean> holder = new Holder<>();
    new MockUp<RestServletInjector>() {
      @Mock
      public Dynamic defaultInject(ServletContext servletContext) {
        holder.value = true;
        return null;
      }
    };

    context.invokeBeanFactoryPostProcessors(beanFactory);

    Assertions.assertTrue(holder.value);
  }
}
