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

package io.servicecomb.core.definition.schema;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.servicecomb.core.Const;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.provider.producer.ProducerOperation;
import io.servicecomb.foundation.common.utils.ReflectUtils;
import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.SwaggerGenerator;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapperFactory;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.util.Yaml;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月5日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class ProducerSchemaFactory extends AbstractSchemaFactory<ProducerSchemaContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerSchemaFactory.class);

    private ObjectWriter writer = Yaml.pretty();

    @Inject
    private ProducerResponseMapperFactory responseMapperFactory;

    @Inject
    protected ProducerArgumentsMapperFactory producerArgsMapperFactory;

    private String getSwaggerContent(Swagger swagger) {
        try {
            return writer.writeValueAsString(swagger);
        } catch (JsonProcessingException e) {
            throw new Error(e);
        }
    }

    // 只会在启动流程中调用
    public SchemaMeta getOrCreateProducerSchema(String microserviceName, String schemaId,
            Class<?> producerClass,
            Object producerInstance) {
        MicroserviceMeta microserviceMeta = microserviceMetaManager.getOrCreateMicroserviceMeta(microserviceName);

        ProducerSchemaContext context = new ProducerSchemaContext();
        context.setMicroserviceMeta(microserviceMeta);
        context.setSchemaId(schemaId);
        context.setProviderClass(producerClass);
        context.setProducerInstance(producerInstance);

        return getOrCreateSchema(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void connectToProvider(ProducerSchemaContext context) {
        if (context.getGenerator() == null) {
            generateSwagger(context);
        }

        // 建立契约与producer之间的参数映射关系
        Class<?> swaggerIntf = ClassUtils.getJavaInterface(context.getSchemaMeta().getSwagger());

        for (OperationMeta operationMeta : context.getSchemaMeta().getOperations()) {
            OperationGenerator operationGenerator =
                context.getGenerator().getOperationGeneratorMap().get(operationMeta.getOperationId());

            Method swaggerMethod = ReflectUtils.findMethod(swaggerIntf, operationMeta.getOperationId());
            List<Parameter> swaggerParameters = operationMeta.getSwaggerOperation().getParameters();

            Method producerMethod = operationGenerator.getProviderMethod();
            List<Parameter> producerParameters = operationGenerator.getProviderParameters();

            ProducerArgumentsMapper argsMapper =
                producerArgsMapperFactory.createArgumentsMapper(context.getSchemaMeta().getSwagger(),
                        swaggerMethod,
                        swaggerParameters,
                        producerMethod,
                        producerParameters);

            createOperation(context, operationMeta, argsMapper);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SwaggerGenerator generateSwagger(ProducerSchemaContext context) {
        SwaggerGenerator generator = super.generateSwagger(context);
        context.setGenerator(generator);

        return generator;
    }

    protected SchemaMeta createSchema(ProducerSchemaContext context) {
        // 尝试从规划的目录加载契约
        Swagger swagger = loadSwagger(context);

        // 根据class动态产生契约
        SwaggerGenerator generator = generateSwagger(context);
        if (swagger == null) {
            swagger = generator.getSwagger();
            String swaggerContent = getSwaggerContent(swagger);
            LOGGER.info("generate swagger for {}/{}/{}, swagger: {}",
                    context.getMicroserviceMeta().getAppId(),
                    context.getMicroserviceName(),
                    context.getSchemaId(),
                    swaggerContent);
        }

        // 注册契约
        return schemaLoader.registerSchema(context.getMicroserviceMeta(), context.getSchemaId(), swagger);
    }

    protected void createOperation(ProducerSchemaContext context, OperationMeta operationMeta,
            ProducerArgumentsMapper argsMapper) {
        Object producerInstance = context.getProducerInstance();
        Method method = ReflectUtils.findMethod(producerInstance.getClass(), operationMeta.getMethod().getName());

        ProducerResponseMapper responseMapper = responseMapperFactory.createResponseMapper(method.getReturnType());
        ProducerOperation producerOperation =
            new ProducerOperation(producerInstance, method, argsMapper, responseMapper);
        operationMeta.putExtData(Const.PRODUCER_OPERATION, producerOperation);
    }

}
