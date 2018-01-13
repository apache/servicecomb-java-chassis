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

package org.apache.servicecomb.config;

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

public class TestLastPropertyPlaceholderConfigurer {
  @Component
  static class Bean extends PropertyPlaceholderConfigurer implements EmbeddedValueResolverAware {
    StringValueResolver resolver;

    public Bean() {
      setIgnoreUnresolvablePlaceholders(true);
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
      this.resolver = resolver;
    }

    @Override
    protected Properties mergeProperties() throws IOException {
      Properties properties = super.mergeProperties();
      properties.put("a", "aValue");
      return properties;
    }
  }

  @Test
  public void test() {
    AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(this.getClass().getPackage().getName());
    Bean bean = context.getBean(Bean.class);

    Assert.assertEquals("aValue", bean.resolver.resolveStringValue("${a}"));
    try {
      bean.resolver.resolveStringValue("${b}");
      Assert.fail("must throw exception");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Could not resolve placeholder 'b' in string value \"${b}\"", e.getMessage());
    }

    context.close();
  }
}
