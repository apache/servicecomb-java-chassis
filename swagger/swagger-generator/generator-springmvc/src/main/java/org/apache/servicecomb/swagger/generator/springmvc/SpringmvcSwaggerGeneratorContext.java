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

package org.apache.servicecomb.swagger.generator.springmvc;

import java.lang.reflect.Method;

import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.apache.servicecomb.swagger.generator.rest.RestSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.CookieValueAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.DeleteMappingMethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.GetMappingMethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.PatchMappingMethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.PathVariableAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.PostMappingMethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.PutMappingMethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.RequestAttributeAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.RequestBodyAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.RequestHeaderAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.RequestMappingClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.RequestMappingMethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.RequestParamAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.annotation.RequestPartAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.springmvc.processor.parameter.SpringmvcDefaultParameterProcessor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

public class SpringmvcSwaggerGeneratorContext extends RestSwaggerGeneratorContext {
  private static final int ORDER = 1000;

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public boolean canProcess(Class<?> cls) {
    return ClassUtils.hasAnnotation(cls, RequestMapping.class);
  }

  @Override
  public boolean canProcess(Method method) {
    return method.getAnnotation(RequestMapping.class) != null ||
        method.getAnnotation(GetMapping.class) != null ||
        method.getAnnotation(PutMapping.class) != null ||
        method.getAnnotation(PostMapping.class) != null ||
        method.getAnnotation(PatchMapping.class) != null ||
        method.getAnnotation(DeleteMapping.class) != null;
  }

  @Override
  protected void initClassAnnotationMgr() {
    super.initClassAnnotationMgr();

    classAnnotationMgr.register(RequestMapping.class, new RequestMappingClassAnnotationProcessor());
  }

  @Override
  protected void initMethodAnnotationMgr() {
    super.initMethodAnnotationMgr();

    methodAnnotationMgr.register(RequestMapping.class, new RequestMappingMethodAnnotationProcessor());
    methodAnnotationMgr.register(GetMapping.class, new GetMappingMethodAnnotationProcessor());
    methodAnnotationMgr.register(PutMapping.class, new PutMappingMethodAnnotationProcessor());
    methodAnnotationMgr.register(PostMapping.class, new PostMappingMethodAnnotationProcessor());
    methodAnnotationMgr.register(PatchMapping.class, new PatchMappingMethodAnnotationProcessor());
    methodAnnotationMgr.register(DeleteMapping.class, new DeleteMappingMethodAnnotationProcessor());
  }

  @Override
  protected void initParameterAnnotationMgr() {
    super.initParameterAnnotationMgr();

    parameterAnnotationMgr.register(CookieValue.class, new CookieValueAnnotationProcessor());
    parameterAnnotationMgr.register(PathVariable.class, new PathVariableAnnotationProcessor());
    parameterAnnotationMgr.register(RequestBody.class, new RequestBodyAnnotationProcessor());
    parameterAnnotationMgr.register(RequestHeader.class, new RequestHeaderAnnotationProcessor());
    parameterAnnotationMgr.register(RequestParam.class, new RequestParamAnnotationProcessor());
    parameterAnnotationMgr.register(RequestAttribute.class, new RequestAttributeAnnotationProcessor());
    parameterAnnotationMgr.register(RequestPart.class, new RequestPartAnnotationProcessor());
  }

  @Override
  protected void initParameterTypeProcessorMgr() {
    super.initParameterTypeProcessorMgr();
  }

  @Override
  protected void initDefaultParameterProcessor() {
    defaultParameterProcessor = new SpringmvcDefaultParameterProcessor();
  }
}
