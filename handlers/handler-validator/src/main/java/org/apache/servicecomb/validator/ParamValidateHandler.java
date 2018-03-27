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
package org.apache.servicecomb.validator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParamValidateHandler
 * Handler to validate the input request parameters 
 *
 */
public class ParamValidateHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParamValidateHandler.class);

  private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    Object[] args = invocation.getArgs();
    boolean invalid = false;
    List<Object> errList = new ArrayList<>();
    if (null != args) {
      for (Object arg : args) {
        Set<ConstraintViolation<Object>> violations = validator.validate(arg);
        if (violations.size() > 0) {
          invalid = true;
          ParamException paramExcep = null;
          for (ConstraintViolation<Object> constraintViolation : violations) {
            String paramName = constraintViolation.getPropertyPath().toString();
            paramExcep = processErrorAnnotation(arg, paramName);
            int errCode = 400;
            Class<?> errHanlder = null;
            if (null != paramExcep) {
              errCode = paramExcep.errorCode();
              errHanlder = paramExcep.errorHandler();
              try {
                Object instance = errHanlder.newInstance();
                if (instance instanceof InvalidParamException) {
                  InvalidParamException invalidParamException = (InvalidParamException) instance;
                  errList.add(invalidParamException.getExceptionData(arg));
                } else {
                  LOGGER.warn("Failed to load configured error details class " + errHanlder.getCanonicalName()
                      + "; Using the default Error hanlder");
                  processDefaultError(errCode, constraintViolation.getMessage(), paramName, errList);
                }
              } catch (InstantiationException e) {
                LOGGER.warn("Failed to load configured error details class " + errHanlder.getCanonicalName()
                    + "; Using the default Error hanlder");
                processDefaultError(errCode, constraintViolation.getMessage(), paramName, errList);
              }

            } else {
              processDefaultError(errCode, constraintViolation.getMessage(), paramName, errList);
            }

          }
        }
      }
    }

    if (invalid) {
      Response res = Response.create(400, "Invalid parameter(s).", errList);
      asyncResp.complete(res);
    } else {
      invocation.next(asyncResp);
    }
  }

  private void processDefaultError(int errCode, String message, String paramName, List<Object> errList) {
    DefaultParamException defaultErrorInfo = new DefaultParamException(paramName, errCode, message);
    errList.add(defaultErrorInfo);
  }

  private ParamException processErrorAnnotation(Object arg, String paramName) {
    Field[] fields = arg.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (paramName.equals(field.getName()) && field.isAnnotationPresent(ParamException.class)) {
        ParamException paramExcep = field.getAnnotation(ParamException.class);
        return paramExcep;
      }

    }
    return null;
  }

}
