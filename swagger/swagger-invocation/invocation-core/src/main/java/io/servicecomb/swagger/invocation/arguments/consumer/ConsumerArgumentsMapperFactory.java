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

package io.servicecomb.swagger.invocation.arguments.consumer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import io.servicecomb.swagger.invocation.arguments.ArgumentsMapperConfig;
import io.servicecomb.swagger.invocation.arguments.ArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 *
 * @version  [版本号, 2017年4月5日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class ConsumerArgumentsMapperFactory extends ArgumentsMapperFactory {
    @Inject
    @Qualifier("consumer")
    public void setFactoryList(List<ContextArgumentMapperFactory> factoryList) {
        createFactoryMap(factoryList);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <T> T createArgumentsMapper(ArgumentsMapperConfig config) {
        return (T) new ConsumerArgumentsMapper(config.getArgumentMapperList(), config.getSwaggerParameters().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ArgumentMapper createArgumentSame(int swaggerIdx, int consumerIdx) {
        return new ConsumerArgumentSame(consumerIdx, swaggerIdx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ArgumentMapper createBodyFieldArgMapper(ArgumentsMapperConfig config, int swaggerArgIdx,
            Map<Integer, Field> fieldMap) {
        Class<?> swaggerParamType = config.getSwaggerMethod().getParameterTypes()[swaggerArgIdx];
        return new ConsumerArgumentToBodyField(swaggerParamType, swaggerArgIdx, fieldMap);
    }
}
