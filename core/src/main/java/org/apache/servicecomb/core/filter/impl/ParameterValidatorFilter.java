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

import static org.apache.servicecomb.swagger.invocation.InvocationType.PRODUCER;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterMeta;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.hibernate.validator.HibernateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FilterMeta(name = "validator", invocationType = PRODUCER)
public class ParameterValidatorFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterValidatorFilter.class);

  private final ExecutableValidator validator;

  public ParameterValidatorFilter() {
    ValidatorFactory factory =
        Validation.byProvider(HibernateValidator.class)
            .configure()
            .propertyNodeNameProvider(new JacksonPropertyNodeNameProvider())
            .buildValidatorFactory();
    validator = factory.getValidator().forExecutables();
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    SwaggerProducerOperation producerOperation = invocation.getOperationMeta().getSwaggerProducerOperation();
    Object instance = producerOperation.getProducerInstance();
    Method method = producerOperation.getProducerMethod();
    Object[] args = invocation.toProducerArguments();
    Set<ConstraintViolation<Object>> violations = validator.validateParameters(instance, method, args, Default.class);
    if (violations.size() > 0) {
      LOGGER.error("Parameter validation failed : " + violations.toString());
      return AsyncUtils.completeExceptionally(new ConstraintViolationException(violations));
    }

    return nextNode.onFilter(invocation);
  }
}
