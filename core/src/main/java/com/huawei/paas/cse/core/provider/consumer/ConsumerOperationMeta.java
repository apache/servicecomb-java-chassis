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

package com.huawei.paas.cse.core.provider.consumer;

import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.swagger.invocation.arguments.consumer.ConsumerArgumentsMapper;
import com.huawei.paas.cse.swagger.invocation.response.consumer.ConsumerResponseMapper;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月1日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ConsumerOperationMeta {
    private OperationMeta operationMeta;

    private ConsumerArgumentsMapper argsMapper;

    private ConsumerResponseMapper responseMapper;

    public ConsumerOperationMeta(OperationMeta operationMeta, ConsumerArgumentsMapper argsMapper,
            ConsumerResponseMapper responseMapper) {
        this.operationMeta = operationMeta;
        this.argsMapper = argsMapper;
        this.responseMapper = responseMapper;
    }

    /**
     * 获取operationMeta的值
     * @return 返回 operationMeta
     */
    public OperationMeta getOperationMeta() {
        return operationMeta;
    }

    /**
     * 获取argsMapper的值
     * @return 返回 argsMapper
     */
    public ConsumerArgumentsMapper getArgsMapper() {
        return argsMapper;
    }

    /**
     * 获取responseMapper的值
     * @return 返回 responseMapper
     */
    public ConsumerResponseMapper getResponseMapper() {
        return responseMapper;
    }
}
