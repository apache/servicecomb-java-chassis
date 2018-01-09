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

package io.servicecomb.metrics.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import com.google.common.collect.Lists;

import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.metrics.common.DefaultHealthCheckExtraData;
import io.servicecomb.metrics.common.HealthCheckResult;
import io.servicecomb.metrics.core.publish.DefaultHealthCheckerManager;
import io.servicecomb.metrics.core.publish.HealthCheckerManager;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import mockit.Expectations;

public class TestHealthCheckerManager {

  @Test
  public void testRegistry() throws IOException {

    Microservice microservice = new Microservice();
    microservice.setAppId("appId");
    microservice.setServiceName("serviceName");
    microservice.setVersion("0.0.1");

    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setEndpoints(Lists.newArrayList("127.0.0.1", "192.168.0.100"));
    microserviceInstance.setInstanceId("001");
    microserviceInstance.setHostName("localhost");

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroservice();
        result = microservice;
      }
    };

    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getMicroserviceInstance();
        result = microserviceInstance;
      }
    };

    HealthCheckerManager manager = new DefaultHealthCheckerManager(new ApplicationContext() {
      @Override
      public String getId() {
        return null;
      }

      @Override
      public String getApplicationName() {
        return null;
      }

      @Override
      public String getDisplayName() {
        return null;
      }

      @Override
      public long getStartupDate() {
        return 0;
      }

      @Override
      public ApplicationContext getParent() {
        return null;
      }

      @Override
      public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
        return null;
      }

      @Override
      public BeanFactory getParentBeanFactory() {
        return null;
      }

      @Override
      public boolean containsLocalBean(String s) {
        return false;
      }

      @Override
      public boolean containsBeanDefinition(String s) {
        return false;
      }

      @Override
      public int getBeanDefinitionCount() {
        return 0;
      }

      @Override
      public String[] getBeanDefinitionNames() {
        return new String[0];
      }

      @Override
      public String[] getBeanNamesForType(ResolvableType resolvableType) {
        return new String[0];
      }

      @Override
      public String[] getBeanNamesForType(Class<?> aClass) {
        return new String[0];
      }

      @Override
      public String[] getBeanNamesForType(Class<?> aClass, boolean b, boolean b1) {
        return new String[0];
      }

      @Override
      public <T> Map<String, T> getBeansOfType(Class<T> aClass) throws BeansException {
        return null;
      }

      @Override
      public <T> Map<String, T> getBeansOfType(Class<T> aClass, boolean b, boolean b1) throws BeansException {
        return null;
      }

      @Override
      public String[] getBeanNamesForAnnotation(Class<? extends Annotation> aClass) {
        return new String[0];
      }

      @Override
      public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> aClass) throws BeansException {
        return null;
      }

      @Override
      public <A extends Annotation> A findAnnotationOnBean(String s, Class<A> aClass)
          throws NoSuchBeanDefinitionException {
        return null;
      }

      @Override
      public Object getBean(String s) throws BeansException {
        return null;
      }

      @Override
      public <T> T getBean(String s, Class<T> aClass) throws BeansException {
        return null;
      }

      @Override
      public <T> T getBean(Class<T> aClass) throws BeansException {
        return null;
      }

      @Override
      public Object getBean(String s, Object... objects) throws BeansException {
        return null;
      }

      @Override
      public <T> T getBean(Class<T> aClass, Object... objects) throws BeansException {
        return null;
      }

      @Override
      public boolean containsBean(String s) {
        return false;
      }

      @Override
      public boolean isSingleton(String s) throws NoSuchBeanDefinitionException {
        return false;
      }

      @Override
      public boolean isPrototype(String s) throws NoSuchBeanDefinitionException {
        return false;
      }

      @Override
      public boolean isTypeMatch(String s, ResolvableType resolvableType) throws NoSuchBeanDefinitionException {
        return false;
      }

      @Override
      public boolean isTypeMatch(String s, Class<?> aClass) throws NoSuchBeanDefinitionException {
        return false;
      }

      @Override
      public Class<?> getType(String s) throws NoSuchBeanDefinitionException {
        return null;
      }

      @Override
      public String[] getAliases(String s) {
        return new String[0];
      }

      @Override
      public void publishEvent(ApplicationEvent applicationEvent) {

      }

      @Override
      public void publishEvent(Object o) {

      }

      @Override
      public String getMessage(String s, Object[] objects, String s1, Locale locale) {
        return null;
      }

      @Override
      public String getMessage(String s, Object[] objects, Locale locale) throws NoSuchMessageException {
        return null;
      }

      @Override
      public String getMessage(MessageSourceResolvable messageSourceResolvable, Locale locale)
          throws NoSuchMessageException {
        return null;
      }

      @Override
      public Environment getEnvironment() {
        return null;
      }

      @Override
      public Resource[] getResources(String s) throws IOException {
        return new Resource[0];
      }

      @Override
      public Resource getResource(String s) {
        return null;
      }

      @Override
      public ClassLoader getClassLoader() {
        return null;
      }
    });
    Map<String, HealthCheckResult> results = manager.check();

    Assert.assertTrue(results.get("default").isHealthy());

    DefaultHealthCheckExtraData data = JsonUtils.OBJ_MAPPER
        .readValue(results.get("default").getExtraData(), DefaultHealthCheckExtraData.class);
    Assert.assertTrue(data.getAppId().equals("appId"));
    Assert.assertTrue(data.getServiceName().equals("serviceName"));
    Assert.assertTrue(data.getServiceVersion().equals("0.0.1"));
    Assert.assertTrue(data.getInstanceId().equals("001"));
    Assert.assertTrue(data.getHostName().equals("localhost"));
    Assert.assertTrue(data.getEndpoints().equals("127.0.0.1,192.168.0.100"));
  }
}
