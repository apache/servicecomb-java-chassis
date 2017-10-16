/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.swagger.generator.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;

import io.servicecomb.foundation.common.RegisterManager;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import io.servicecomb.swagger.extend.annotations.RawJsonRequestBody;
import io.servicecomb.swagger.extend.annotations.ResponseHeaders;
import io.servicecomb.swagger.generator.core.AnnotationProcessorManager.AnnotationType;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiImplicitParamClassProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiImplicitParamMethodProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiImplicitParamsClassProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiImplicitParamsMethodProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiOperationProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiResponseClassProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiResponseMethodProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiResponsesClassProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ApiResponsesMethodProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ResponseHeaderProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.ResponseHeadersProcessor;
import io.servicecomb.swagger.generator.core.processor.annotation.SwaggerDefinitionProcessor;
import io.servicecomb.swagger.generator.core.processor.parametertype.RawJsonRequestBodyProcessor;
import io.servicecomb.swagger.generator.core.processor.response.DefaultResponseTypeProcessor;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;

/**
 * 根据class反向生成swagger的上下文对象
 */
public abstract class AbstractSwaggerGeneratorContext implements SwaggerGeneratorContext, EmbeddedValueResolverAware {
  protected StringValueResolver stringValueResolver;

  protected AnnotationProcessorManager<ClassAnnotationProcessor> classAnnotationMgr =
      new AnnotationProcessorManager<>(AnnotationType.CLASS);

  protected AnnotationProcessorManager<MethodAnnotationProcessor> methodAnnotationMgr =
      new AnnotationProcessorManager<>(AnnotationType.METHOD);

  protected AnnotationProcessorManager<ParameterAnnotationProcessor> parameterAnnotationMgr =
      new AnnotationProcessorManager<>(AnnotationType.PARAMETER);

  // 管理方法入参的processor，用于支撑httpRequest、Context之类的特殊处理
  // key为class
  protected RegisterManager<Type, ParameterTypeProcessor> parameterTypeProcessorMgr =
      new RegisterManager<>("parameter type processor mgr");

  protected DefaultParameterProcessor defaultParameterProcessor;

  protected RegisterManager<Type, ResponseTypeProcessor> responseTypeProcessorMgr =
      new RegisterManager<>("response type processor mgr");

  protected ResponseTypeProcessor defaultResponseTypeProcessor = new DefaultResponseTypeProcessor();

  public AbstractSwaggerGeneratorContext() {
    initClassAnnotationMgr();
    initMethodAnnotationMgr();
    initParameterAnnotationMgr();

    initParameterTypeProcessorMgr();

    initDefaultParameterProcessor();

    initResponseTypeProcessorMgr();
  }

  @Override
  public void setEmbeddedValueResolver(StringValueResolver resolver) {
    this.stringValueResolver = resolver;
  }

  @Override
  public String resolveStringValue(String strVal) {
    if (stringValueResolver == null) {
      return strVal;
    }

    return stringValueResolver.resolveStringValue(strVal);
  }

  protected void initClassAnnotationMgr() {
    classAnnotationMgr.register(SwaggerDefinition.class, new SwaggerDefinitionProcessor());

    classAnnotationMgr.register(ApiImplicitParams.class, new ApiImplicitParamsClassProcessor());
    classAnnotationMgr.register(ApiImplicitParam.class, new ApiImplicitParamClassProcessor());

    classAnnotationMgr.register(ApiResponses.class, new ApiResponsesClassProcessor());
    classAnnotationMgr.register(ApiResponse.class, new ApiResponseClassProcessor());
  }

  protected void initMethodAnnotationMgr() {
    methodAnnotationMgr.register(ApiOperation.class, new ApiOperationProcessor());

    methodAnnotationMgr.register(ApiImplicitParams.class, new ApiImplicitParamsMethodProcessor());
    methodAnnotationMgr.register(ApiImplicitParam.class, new ApiImplicitParamMethodProcessor());

    methodAnnotationMgr.register(ApiResponses.class, new ApiResponsesMethodProcessor());
    methodAnnotationMgr.register(ApiResponse.class, new ApiResponseMethodProcessor());

    methodAnnotationMgr.register(ResponseHeaders.class, new ResponseHeadersProcessor());
    methodAnnotationMgr.register(ResponseHeader.class, new ResponseHeaderProcessor());
  }

  protected void initParameterAnnotationMgr() {
    parameterAnnotationMgr.register(RawJsonRequestBody.class, new RawJsonRequestBodyProcessor());
  }

  protected void initParameterTypeProcessorMgr() {
    SPIServiceUtils.getAllService(CommonParameterTypeProcessor.class).forEach(p -> {
      parameterTypeProcessorMgr.register(p.getParameterType(), p);
    });
  }

  protected void initDefaultParameterProcessor() {
  }

  protected void initResponseTypeProcessorMgr() {

  }

  public void setDefaultParamProcessor(DefaultParameterProcessor defaultParamProcessor) {
    this.defaultParameterProcessor = defaultParamProcessor;
  }

  public ClassAnnotationProcessor findClassAnnotationProcessor(Class<? extends Annotation> annotationType) {
    return classAnnotationMgr.findProcessor(annotationType);
  }

  public MethodAnnotationProcessor findMethodAnnotationProcessor(Class<? extends Annotation> annotationType) {
    return methodAnnotationMgr.findProcessor(annotationType);
  }

  public ParameterAnnotationProcessor findParameterAnnotationProcessor(Class<? extends Annotation> annotationType) {
    return parameterAnnotationMgr.findProcessor(annotationType);
  }

  public ParameterTypeProcessor findParameterTypeProcessor(Type type) {
    return parameterTypeProcessorMgr.findValue(type);
  }

  public DefaultParameterProcessor getDefaultParamProcessor() {
    return defaultParameterProcessor;
  }

  @Override
  public ResponseTypeProcessor findResponseTypeProcessor(Type responseType) {
    ResponseTypeProcessor processor = responseTypeProcessorMgr.findValue(responseType);
    if (processor == null) {
      processor = defaultResponseTypeProcessor;
    }

    return processor;
  }
}
