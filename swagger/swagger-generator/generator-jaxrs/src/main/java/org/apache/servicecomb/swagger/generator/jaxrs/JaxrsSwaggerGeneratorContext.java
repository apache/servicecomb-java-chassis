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

package org.apache.servicecomb.swagger.generator.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.BeanParamAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.ConsumesAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.CookieParamAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.FormParamAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.HeaderParamAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.HttpMethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.PathClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.PathMethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.PathParamAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.ProducesAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation.QueryParamAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.jaxrs.processor.parameter.JaxrsDefaultParameterProcessor;
import org.apache.servicecomb.swagger.generator.rest.RestSwaggerGeneratorContext;

public class JaxrsSwaggerGeneratorContext extends RestSwaggerGeneratorContext {
  private static final int ORDER = 2000;

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public boolean canProcess(Class<?> cls) {
    return ClassUtils.hasAnnotation(cls, Path.class);
  }

  @Override
  public boolean canProcess(Method method) {
    for (Annotation annotation : method.getAnnotations()) {
      HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
      if (httpMethod != null) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected void initDefaultParameterProcessor() {
    defaultParameterProcessor = new JaxrsDefaultParameterProcessor();
  }

  @Override
  protected void initClassAnnotationMgr() {
    super.initClassAnnotationMgr();

    classAnnotationMgr.register(Path.class, new PathClassAnnotationProcessor());
  }

  @Override
  protected void initMethodAnnotationMgr() {
    super.initMethodAnnotationMgr();

    methodAnnotationMgr.register(Path.class, new PathMethodAnnotationProcessor());
    methodAnnotationMgr.register(Produces.class, new ProducesAnnotationProcessor());
    methodAnnotationMgr.register(Consumes.class, new ConsumesAnnotationProcessor());

    HttpMethodAnnotationProcessor httpMethodProcessor = new HttpMethodAnnotationProcessor();
    methodAnnotationMgr.register(GET.class, httpMethodProcessor);
    methodAnnotationMgr.register(POST.class, httpMethodProcessor);
    methodAnnotationMgr.register(PUT.class, httpMethodProcessor);
    methodAnnotationMgr.register(DELETE.class, httpMethodProcessor);
  }

  @Override
  protected void initParameterAnnotationMgr() {
    super.initParameterAnnotationMgr();

    parameterAnnotationMgr.register(PathParam.class, new PathParamAnnotationProcessor());
    parameterAnnotationMgr.register(FormParam.class, new FormParamAnnotationProcessor());
    parameterAnnotationMgr.register(CookieParam.class, new CookieParamAnnotationProcessor());

    parameterAnnotationMgr.register(HeaderParam.class, new HeaderParamAnnotationProcessor());
    parameterAnnotationMgr.register(QueryParam.class, new QueryParamAnnotationProcessor());
    parameterAnnotationMgr.register(BeanParam.class, new BeanParamAnnotationProcessor());
  }
}
