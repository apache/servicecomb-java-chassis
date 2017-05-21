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

package com.huawei.paas.cse.transport.rest.servlet;

import javax.servlet.http.HttpServletRequest;

import io.servicecomb.swagger.invocation.SwaggerInvocation;
import io.servicecomb.swagger.invocation.arguments.producer.AbstractProducerContextArgMapper;

/**
 * 使用Servlet http request构造其他各种http request
 * @author   
 * @version  [版本号, 2017年1月22日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ProducerServletHttpRequestArgMapper extends AbstractProducerContextArgMapper {

    private HttpServletRequest httpRequest;

    public ProducerServletHttpRequestArgMapper(HttpServletRequest httpRequest) {
        super(-1);
        this.httpRequest = httpRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createContextArg(SwaggerInvocation invocation) {
        return httpRequest;
    }

}
