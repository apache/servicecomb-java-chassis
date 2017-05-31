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

package io.servicecomb.core.definition;

import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @version  [版本号, 2016年12月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class SchemaUtils {

    private SchemaUtils() {
    }

    public static String generatePackageName(MicroserviceMeta microserviceMeta, String schemaId) {
        String name = "cse.gen." + microserviceMeta.getAppId() + "." + microserviceMeta.getShortName() + "."
                + schemaId;

        return ClassUtils.correctClassName(name);
    }

    public static String swaggerToString(Swagger swagger) {
        try {
            return Yaml.mapper().writeValueAsString(swagger);
        } catch (JsonProcessingException e) {
            throw new Error(e);
        }
    }

    public static Swagger parseSwagger(URL url) {
        try {
            String swaggerContext = IOUtils.toString(url);
            return Yaml.mapper().readValue(swaggerContext, Swagger.class);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Swagger parseSwagger(String swaggerContent) {
        try {
            return Yaml.mapper().readValue(swaggerContent, Swagger.class);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
