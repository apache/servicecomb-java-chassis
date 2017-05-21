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

package io.servicecomb.swagger.generator.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import io.servicecomb.swagger.generator.core.DefaultSwaggerGeneratorContext;
import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.ConsumesAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.CookieParamAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.FormParamAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.HeaderParamAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.HttpMethodAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.PathClassAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.PathMethodAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.PathParamAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.ProducesAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.annotation.QueryParamAnnotationProcessor;
import io.servicecomb.swagger.generator.jaxrs.processor.response.ResponseProcessor;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author
 * @version  [版本号, 2017年3月27日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class JaxrsSwaggerGeneratorContext extends DefaultSwaggerGeneratorContext {
    private static final int ORDER = 500;

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canProcess(Class<?> cls) {
        return ClassUtils.hasAnnotation(cls, Path.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initClassAnnotationMgr() {
        super.initClassAnnotationMgr();

        classAnnotationMgr.register(Path.class, new PathClassAnnotationProcessor());
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initParameterAnnotationMgr() {
        super.initParameterAnnotationMgr();

        parameterAnnotationMgr.register(PathParam.class, new PathParamAnnotationProcessor());
        parameterAnnotationMgr.register(FormParam.class, new FormParamAnnotationProcessor());
        parameterAnnotationMgr.register(CookieParam.class, new CookieParamAnnotationProcessor());

        parameterAnnotationMgr.register(HeaderParam.class, new HeaderParamAnnotationProcessor());
        parameterAnnotationMgr.register(QueryParam.class, new QueryParamAnnotationProcessor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initResponseTypeProcessorMgr() {
        super.initResponseTypeProcessorMgr();

        responseTypeProcessorMgr.register(Response.class, new ResponseProcessor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void correctPath(OperationGenerator operationGenerator) {
        String path = operationGenerator.getPath();
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
        operationGenerator.setPath(path);
    }
}
