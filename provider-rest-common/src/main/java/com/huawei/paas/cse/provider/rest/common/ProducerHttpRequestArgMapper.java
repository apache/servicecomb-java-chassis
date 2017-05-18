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

package com.huawei.paas.cse.provider.rest.common;

import com.huawei.paas.cse.common.rest.RestConst;
import com.huawei.paas.cse.core.Invocation;
import com.huawei.paas.cse.swagger.invocation.SwaggerInvocation;
import com.huawei.paas.cse.swagger.invocation.arguments.producer.AbstractProducerContextArgMapper;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年1月24日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ProducerHttpRequestArgMapper extends AbstractProducerContextArgMapper {
    public ProducerHttpRequestArgMapper(int producerArgIdx) {
        super(producerArgIdx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createContextArg(SwaggerInvocation swaggerInvocation) {
        Invocation invocation = (Invocation) swaggerInvocation;
        // 从rest transport来
        AbstractProducerContextArgMapper httpRequestCreator =
            (AbstractProducerContextArgMapper) invocation.getHandlerContext()
                    .get(RestConst.HTTP_REQUEST_CREATOR);
        if (httpRequestCreator != null) {
            return httpRequestCreator.createContextArg(invocation);
        }

        // 通过args模拟request
        return new GenericServletMockRequest(invocation);
    }
}
