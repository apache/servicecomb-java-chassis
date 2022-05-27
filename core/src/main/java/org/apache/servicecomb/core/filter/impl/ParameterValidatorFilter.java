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
package org.apache.servicecomb.core.filter.impl;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProducerFilter;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class ParameterValidatorFilter implements ProducerFilter, InitializingBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterValidatorFilter.class);

  public static final String NAME = "validator";

  private static final String ENABLE_EL = "servicecomb.filters.validation.useResourceBundleMessageInterpolator";

  protected ExecutableValidator validator;

  @Nonnull
  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void afterPropertiesSet() {
    validator = createValidatorFactory()
        .getValidator().forExecutables();
  }

  protected ValidatorFactory createValidatorFactory() {
    return Validation.byProvider(HibernateValidator.class)
        .configure()
        .propertyNodeNameProvider(new JacksonPropertyNodeNameProvider())
        .messageInterpolator(messageInterpolator())
        .buildValidatorFactory();
  }

  protected AbstractMessageInterpolator messageInterpolator() {
    if (useResourceBundleMessageInterpolator()) {
      return new ResourceBundleMessageInterpolator();
    }
    return new ParameterMessageInterpolator();
  }

  private boolean useResourceBundleMessageInterpolator() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(ENABLE_EL, false).get();
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    Set<ConstraintViolation<Object>> violations = doValidate(invocation);
    if (violations.size() > 0) {
      LOGGER.error("Parameter validation failed : " + violations);
      return AsyncUtils.completeExceptionally(new ConstraintViolationException(violations));
    }

    return nextNode.onFilter(invocation);
  }

  protected Set<ConstraintViolation<Object>> doValidate(Invocation invocation) {
    SwaggerProducerOperation producerOperation = invocation.getOperationMeta().getSwaggerProducerOperation();
    Object instance = producerOperation.getProducerInstance();
    Method method = producerOperation.getProducerMethod();
    Object[] args = invocation.toProducerArguments();
    return validator.validateParameters(instance, method, args, Default.class);
  }
}
