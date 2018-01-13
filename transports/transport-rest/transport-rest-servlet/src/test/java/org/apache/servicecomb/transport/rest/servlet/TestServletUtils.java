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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestServletUtils {
  @Test
  public void testCheckUrlPatternNormal() {
    ServletUtils.checkUrlPattern("/*");
    ServletUtils.checkUrlPattern("/abc/*");
    ServletUtils.checkUrlPattern("/abc/def/*");

    // normal, must not throw exception, no need to check
  }

  @Test
  public void testCheckUrlPatternMiddleWideChar() {
    try {
      ServletUtils.checkUrlPattern("/abc/*def");
      Assert.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assert.assertEquals("only support rule like /* or /path/* or /path1/path2/* and so on.", e.getMessage());
    }
  }

  @Test
  public void testCheckUrlPatternNoWideChar() {
    try {
      ServletUtils.checkUrlPattern("/abcdef");
      Assert.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assert.assertEquals("only support rule like /* or /path/* or /path1/path2/* and so on.", e.getMessage());
    }
  }

  @Test
  public void testCheckUrlPatternNotStartWithSlash() {
    try {
      ServletUtils.checkUrlPattern("abcdef/*");
      Assert.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assert.assertEquals("only support rule like /* or /path/* or /path1/path2/* and so on.", e.getMessage());
    }
  }

  @Test
  public void testFilterUrlPatternsNormal() {
    String urlPattern = "/r1/*";

    Collection<String> urlPatterns = Arrays.asList(urlPattern);
    String[] result = ServletUtils.filterUrlPatterns(urlPatterns);
    Assert.assertThat(result, Matchers.arrayContaining("/r1/*"));

    result = ServletUtils.filterUrlPatterns(urlPattern);
    Assert.assertThat(result, Matchers.arrayContaining("/r1/*"));
  }

  @Test
  public void testFilterUrlPatternsEmpty() {
    Collection<String> urlPatterns = Arrays.asList(" ", "\t");
    String[] result = ServletUtils.filterUrlPatterns(urlPatterns);
    Assert.assertThat(result, Matchers.emptyArray());
  }

  @Test
  public void testFilterUrlPatternsInvalid() {
    Collection<String> urlPatterns = Arrays.asList("/abc");
    try {
      ServletUtils.filterUrlPatterns(urlPatterns);
      Assert.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assert.assertEquals("only support rule like /* or /path/* or /path1/path2/* and so on.", e.getMessage());
    }
  }

  @Test
  public void testcollectUrlPatternsNoRestServlet(@Mocked ServletContext servletContext,
      @Mocked ServletRegistration servletRegistration) {
    new Expectations() {
      {
        servletRegistration.getClassName();
        result = "test";
        servletContext.getServletRegistrations();
        result = Collections.singletonMap("test", servletRegistration);
      }
    };

    String[] result = ServletUtils.collectUrlPatterns(servletContext, RestServlet.class);
    Assert.assertThat(result, Matchers.emptyArray());
  }

  @Test
  public void testcollectUrlPatternsNormalMapping(@Mocked ServletContext servletContext,
      @Mocked ServletRegistration r1, @Mocked ServletRegistration r2) {
    Map<String, ServletRegistration> servletRegistrationMap = new LinkedHashMap<>();
    servletRegistrationMap.put("r1", r1);
    servletRegistrationMap.put("r2", r2);

    new Expectations() {
      {
        r1.getClassName();
        result = RestServlet.class.getName();
        r1.getMappings();
        result = Arrays.asList("/r1/*", "/r1/1/*");

        r2.getClassName();
        result = RestServlet.class.getName();

        servletContext.getServletRegistrations();
        result = servletRegistrationMap;
      }
    };

    String[] result = ServletUtils.collectUrlPatterns(servletContext, RestServlet.class);
    Assert.assertThat(result, Matchers.arrayContaining("/r1/*", "/r1/1/*"));
  }

  @Test
  public void testSaveUrlPrefixNull(@Mocked ServletContext servletContext) {
    System.clearProperty(Const.URL_PREFIX);

    ServletUtils.saveUrlPrefix(servletContext);

    Assert.assertNull(System.getProperty(Const.URL_PREFIX));
    System.clearProperty(Const.URL_PREFIX);
  }

  @Test
  public void testSaveUrlPrefixNormal(@Mocked ServletContext servletContext,
      @Mocked ServletRegistration servletRegistration) {
    System.clearProperty(Const.URL_PREFIX);
    new Expectations() {
      {
        servletContext.getContextPath();
        result = "/root";
        servletRegistration.getClassName();
        result = RestServlet.class.getName();
        servletRegistration.getMappings();
        result = Arrays.asList("/rest/*");
        servletContext.getServletRegistrations();
        result = Collections.singletonMap("test", servletRegistration);
      }
    };

    ServletUtils.saveUrlPrefix(servletContext);

    Assert.assertThat(System.getProperty(Const.URL_PREFIX), Matchers.is("/root/rest"));
    System.clearProperty(Const.URL_PREFIX);
  }
}
