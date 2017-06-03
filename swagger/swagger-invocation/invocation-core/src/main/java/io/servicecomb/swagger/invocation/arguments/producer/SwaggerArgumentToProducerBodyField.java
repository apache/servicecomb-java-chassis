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

package io.servicecomb.swagger.invocation.arguments.producer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import io.servicecomb.swagger.invocation.SwaggerInvocation;
import io.servicecomb.swagger.invocation.arguments.ArgumentMapper;

/**
 * 透明RPC的典型场景
 * 因为没有标注指明RESTful映射方式
 * 所以，所有参数被包装为一个class，每个参数是一个field
 * producer在处理时，需要将这些field取出来当作参数使用
 */
public class SwaggerArgumentToProducerBodyField implements ArgumentMapper {
    private int swaggerIdx;

    // key为producerArgs的下标
    private Map<Integer, Field> fieldMap;

    public SwaggerArgumentToProducerBodyField(int swaggerIdx, Map<Integer, Field> fieldMap) {
        this.swaggerIdx = swaggerIdx;
        this.fieldMap = fieldMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mapArgument(SwaggerInvocation invocation, Object[] producerArguments) {
        Object body = invocation.getSwaggerArgument(swaggerIdx);

        try {
            for (Entry<Integer, Field> entry : fieldMap.entrySet()) {
                Object fieldValue = entry.getValue().get(body);
                producerArguments[entry.getKey()] = fieldValue;
            }
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
