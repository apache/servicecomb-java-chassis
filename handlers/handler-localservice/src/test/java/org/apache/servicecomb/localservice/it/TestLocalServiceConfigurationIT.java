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

package org.apache.servicecomb.localservice.it;

import java.util.Map;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.localservice.LocalServiceBootListener;
import org.apache.servicecomb.localservice.LocalServiceConfig;
import org.apache.servicecomb.localservice.LocalServiceConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Integration test: verify the Spring auto configuration of the module.
 *
 * <p>In Java Chassis 3.x BootListener instances are collected from the Spring
 * context, so the local service boot listener must be registered as a bean and
 * respect {@code servicecomb.localservice.enabled}.
 *
 * <p>Run explicitly: {@code mvn test -Dtest=*IT -pl handlers/handler-localservice}
 */
public class TestLocalServiceConfigurationIT {
  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(LocalServiceConfiguration.class));

  @AfterEach
  public void tearDown() {
    // make sure a stale MockEnvironment never leaks into other tests
    org.apache.servicecomb.foundation.common.LegacyPropertyFactory.setEnvironment(null);
  }

  @Test
  public void enabledRegistersBootListenerBean() {
    runner.withPropertyValues(LocalServiceConfig.KEY_ENABLED + "=true")
        .run(context -> {
          Assertions.assertTrue(context.containsBean("localServiceBootListener"));
          LocalServiceBootListener listener = context.getBean(LocalServiceBootListener.class);
          Assertions.assertNotNull(listener);
          // the bean must be discoverable as a generic BootListener, because
          // that is how the engine collects boot listeners
          Map<String, BootListener> listeners = context.getBeansOfType(BootListener.class);
          Assertions.assertTrue(listeners.containsKey("localServiceBootListener"));
        });
  }

  @Test
  public void disabledByDefaultRegistersNoBean() {
    runner.run(context -> {
      Assertions.assertFalse(context.containsBean("localServiceBootListener"));
      Assertions.assertThrows(NoSuchBeanDefinitionException.class,
          () -> context.getBean(LocalServiceBootListener.class));
    });
  }

  @Test
  public void explicitlyDisabledRegistersNoBean() {
    runner.withPropertyValues(LocalServiceConfig.KEY_ENABLED + "=false")
        .run(context -> Assertions.assertFalse(context.containsBean("localServiceBootListener")));
  }

  @Test
  public void nonBooleanValueRegistersNoBean() {
    // @ConditionalOnProperty requires the exact value "true"
    runner.withPropertyValues(LocalServiceConfig.KEY_ENABLED + "=yes")
        .run(context -> Assertions.assertFalse(context.containsBean("localServiceBootListener")));
  }

  @Test
  public void plainAnnotationContextHonorsCondition() {
    // the engine may build its own context: verify the condition also works
    // without the boot test infrastructure
    Map<String, Object> source = Map.of(LocalServiceConfig.KEY_ENABLED, "true");
    org.springframework.core.env.MapPropertySource propertySource =
        new org.springframework.core.env.MapPropertySource("test", source);
    org.springframework.core.env.StandardEnvironment environment = new org.springframework.core.env.StandardEnvironment();
    environment.getPropertySources().addFirst(propertySource);

    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.setEnvironment(environment);
      context.register(LocalServiceConfiguration.class);
      context.refresh();
      Assertions.assertNotNull(context.getBean(LocalServiceBootListener.class));
    }

    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.register(LocalServiceConfiguration.class);
      context.refresh();
      Assertions.assertFalse(context.containsBean("localServiceBootListener"));
    }
  }

  @Test
  public void autoConfigurationImportsFileContainsLocalServiceConfiguration() throws Exception {
    String path = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";
    try (var in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
      Assertions.assertNotNull(in, "AutoConfiguration.imports not on test classpath");
      String content = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
      Assertions.assertTrue(content.contains(LocalServiceConfiguration.class.getName()),
          "AutoConfiguration.imports must register " + LocalServiceConfiguration.class.getName());
    }
  }
}
