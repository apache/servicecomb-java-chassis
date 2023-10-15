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
package org.apache.servicecomb.swagger.invocation.validator;

import java.util.Set;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.extension.ProducerInvokeExtension;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.groups.Default;

public class ParameterValidator implements ProducerInvokeExtension {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterValidator.class);

  private static final String PARAM_VALIDATION_ENABLED = "servicecomb.rest.parameter.validation.enabled";

  private static final String ENABLE_EL = "servicecomb.filters.validation.useResourceBundleMessageInterpolator";

  private final boolean paramValidationEnabled;

  public ParameterValidator() {
    paramValidationEnabled = LegacyPropertyFactory.getBooleanProperty(PARAM_VALIDATION_ENABLED, true);
  }

  private static ExecutableValidator executableValidator;

  @Override
  public void beforeMethodInvoke(SwaggerInvocation invocation, SwaggerProducerOperation producerOperation,
      Object[] args)
      throws ConstraintViolationException {
    if (paramValidationEnabled) {
      if (null == executableValidator) {
        ValidatorFactory factory =
            Validation.byDefaultProvider()
                .configure()
                .parameterNameProvider(new DefaultParameterNameProvider())
                .messageInterpolator(messageInterpolator())
                .buildValidatorFactory();
        executableValidator = factory.getValidator().forExecutables();
      }
      Set<ConstraintViolation<Object>> violations =
          executableValidator.validateParameters(producerOperation.getProducerInstance(),
              producerOperation.getProducerMethod(),
              args,
              Default.class);
      if (violations.size() > 0) {
        LOGGER.warn("Parameter validation failed : " + violations);
        throw new ConstraintViolationException(violations);
      }
    }
  }

  private AbstractMessageInterpolator messageInterpolator() {
    if (useResourceBundleMessageInterpolator()) {
      return new ResourceBundleMessageInterpolator();
    }
    return new ParameterMessageInterpolator();
  }

  private boolean useResourceBundleMessageInterpolator() {
    return LegacyPropertyFactory.getBooleanProperty(ENABLE_EL, false);
  }

  @Override
  public int getOrder() {
    return 100;
  }
}
