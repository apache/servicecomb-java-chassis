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

package com.huawei.paas.cse.swagger.invocation.arguments;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.huawei.paas.cse.swagger.extend.parameter.ContextParameter;

import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月5日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class ArgumentsMapperFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentsMapperFactory.class);

    // key为ContextParameter
    private Map<Class<?>, ContextArgumentMapperFactory> factoryMap = new HashMap<>();

    protected int findInParameters(String parameterName, List<Parameter> parameters) {
        for (int idx = 0; idx < parameters.size(); idx++) {
            Parameter parameter = parameters.get(idx);
            if (parameterName.equals(parameter.getName())) {
                return idx;
            }
        }

        return -1;
    }

    protected void createFactoryMap(List<ContextArgumentMapperFactory> factoryList) {
        factoryList.forEach(factory -> {
            factoryMap.put(factory.getContextClass(), factory);
        });
    }

    protected ContextArgumentMapperFactory findFactory(Class<?> cls) {
        return factoryMap.get(cls);
    }

    public <T> T createArgumentsMapper(Swagger swagger, Method swaggerMethod,
            List<Parameter> swaggerParameters,
            Method providerMethod,
            List<Parameter> providerParameters) {
        ArgumentsMapperConfig config = new ArgumentsMapperConfig();
        config.setSwagger(swagger);
        config.setSwaggerMethod(swaggerMethod);
        config.setSwaggerParameters(swaggerParameters);
        config.setProviderMethod(providerMethod);
        config.setProviderParameters(providerParameters);

        collectContextArgumentsMapper(config);
        collectSwaggerArgumentsMapper(config);

        return createArgumentsMapper(config);
    }

    protected abstract <T> T createArgumentsMapper(ArgumentsMapperConfig config);

    protected abstract ArgumentMapper createArgumentSame(int swaggerIdx, int providerIdx);

    protected abstract ArgumentMapper createBodyFieldArgMapper(ArgumentsMapperConfig config, int swaggerArgIdx,
            Map<Integer, Field> fieldMap);

    protected void collectSwaggerArgumentsMapper(ArgumentsMapperConfig config) {
        for (int swaggerIdx = 0; swaggerIdx < config.getSwaggerParameters().size(); swaggerIdx++) {
            Parameter swaggerParameter = config.getSwaggerParameters().get(swaggerIdx);

            int providerIdx = findInParameters(swaggerParameter.getName(), config.getProviderParameters());
            if (providerIdx >= 0) {
                // 如果body参数，不一定能直接映射
                if (BodyParameter.class.isInstance(swaggerParameter)) {
                    mapBodyArg(config, providerIdx, swaggerIdx);
                    continue;
                }

                // 直接映射
                config.addArgumentMapper(createArgumentSame(swaggerIdx, providerIdx));
                continue;
            }

            if (BodyParameter.class.isInstance(swaggerParameter)) {
                processBodyArgMapper(config, swaggerIdx);
                continue;
            }

            LOGGER.warn(generateSkipParamInfo("parameter", config, swaggerParameter.getName()));
        }
    }

    protected void processBodyArgMapper(ArgumentsMapperConfig config, int swaggerIdx) {
        Parameter swaggerParameter = config.getSwaggerParameters().get(swaggerIdx);

        // 不可以使用fieldMap的个数进行判定，那个里面是用参数名进行匹配的，而body不一定满足这个条件
        List<Integer> providerBodyIndexList = getBodyIndexList(config.getProviderParameters());
        if (providerBodyIndexList.isEmpty()) {
            // 没有匹配项
            LOGGER.warn(generateSkipParamInfo("parameter", config, swaggerParameter.getName()));
            return;
        }

        if (providerBodyIndexList.size() == 1) {
            // 如果provider参数中只有一个body，则不管名字，直接匹配
            int providerBodyArgIdx = providerBodyIndexList.get(0);
            mapBodyArg(config, providerBodyArgIdx, swaggerIdx);
            return;
        }

        // 如果有多个body，则需要按field匹配
        processBodyFieldArgMapper(config, swaggerIdx);
    }

    protected void mapBodyArg(ArgumentsMapperConfig config, int providerBodyArgIdx, int swaggerBodyArgIdx) {
        Type providerParameterType = config.getProviderMethod().getGenericParameterTypes()[providerBodyArgIdx];
        Type swaggerParameterType = config.getSwaggerMethod().getGenericParameterTypes()[swaggerBodyArgIdx];
        if (providerParameterType.equals(swaggerParameterType)) {
            config.addArgumentMapper(createArgumentSame(swaggerBodyArgIdx, providerBodyArgIdx));
            return;
        }

        processBodyFieldArgMapper(config, swaggerBodyArgIdx);
    }

    protected void collectContextArgumentsMapper(ArgumentsMapperConfig config) {
        for (int providerIdx = 0; providerIdx < config.getProviderParameters().size(); providerIdx++) {
            Parameter providerParameter = config.getProviderParameters().get(providerIdx);

            if (!ContextParameter.class.isInstance(providerParameter)) {
                continue;
            }

            ContextArgumentMapperFactory factory = findFactory(providerParameter.getClass());
            if (factory != null) {
                ArgumentMapper mapper = factory.create(providerIdx);
                config.addArgumentMapper(mapper);
                continue;
            }

            throw new Error("unknown context parameter " + providerParameter.getClass().getName());
        }
    }

    // 将多个provider参数映射为契约body的field
    // value为field在provider参数中的下标
    protected void processBodyFieldArgMapper(ArgumentsMapperConfig config, int swaggerIdx) {
        Class<?>[] swaggerParameterTypes = config.getSwaggerMethod().getParameterTypes();
        Class<?> swaggerParameterType = swaggerParameterTypes[swaggerIdx];

        BodyParameter bp = (BodyParameter) config.getSwaggerParameters().get(swaggerIdx);
        Model model = bp.getSchema();

        if (RefModel.class.isInstance(model)) {
            String refName = ((RefModel) model).getSimpleRef();
            model = config.getSwagger().getDefinitions().get(refName);
        }

        Map<Integer, Field> fieldMap = new HashMap<>();
        for (String propertyName : model.getProperties().keySet()) {
            // 理论上应该只在provider的body类型的参数中查找，不过正常定义契约不会有问题的，先不用处理了
            int providerIdx = findInParameters(propertyName, config.getProviderParameters());
            if (providerIdx >= 0) {
                Field field = ReflectionUtils.findField(swaggerParameterType, propertyName);
                field.setAccessible(true);
                fieldMap.put(providerIdx, field);
                continue;
            }

            String msg = generateSkipParamInfo("body parameter field", config, propertyName);
            LOGGER.warn(msg);
        }

        ArgumentMapper bodyFieldArg = createBodyFieldArgMapper(config, swaggerIdx, fieldMap);
        config.addArgumentMapper(bodyFieldArg);
    }

    protected List<Integer> getBodyIndexList(List<Parameter> providerParameters) {
        List<Integer> providerBodyIndexList = new ArrayList<>();
        for (int idx = 0; idx < providerParameters.size(); idx++) {
            Parameter parameter = providerParameters.get(idx);
            if (BodyParameter.class.isInstance(parameter)) {
                providerBodyIndexList.add(idx);
            }
        }
        return providerBodyIndexList;
    }

    protected String generateSkipParamInfo(String parameterDesc, ArgumentsMapperConfig config,
            String swaggerParameterName) {
        List<String> names =
            config.getProviderParameters().stream().map(p -> p.getName()).collect(Collectors.toList());
        return String.format("skip %s %s of swagger %s:%s, not found in provider %s:%s(%s)",
                parameterDesc,
                swaggerParameterName,
                config.getSwaggerMethod().getDeclaringClass().getName(),
                config.getSwaggerMethod().getName(),
                config.getProviderMethod().getDeclaringClass().getName(),
                config.getProviderMethod().getName(),
                names);
    }
}
