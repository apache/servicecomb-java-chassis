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

package io.servicecomb.common.rest.codec.param;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.servicecomb.common.rest.codec.RestClientRequest;
import io.servicecomb.common.rest.codec.RestServerRequest;

/**
 * 用于处理Jaxrs中的Path类型参数
 *
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PathProcessorCreator implements ParamValueProcessorCreator {
    public static final String PARAMTYPE = "path";

    public static class PathProcessor extends AbstractParamProcessor {
        public PathProcessor(String paramPath, JavaType targetType) {
            super(paramPath, targetType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(RestServerRequest request) throws Exception {
            String value = request.getPathParam(paramPath);
            if (value == null) {
                return null;
            }
            return convertValue(value, targetType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
            // path不需要set
        }

        @Override
        public String getProcessorType() {
            return PARAMTYPE;
        }

    }

    public PathProcessorCreator() {
        ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParamValueProcessor create(String paramValue, Type genericParamType) {
        JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
        return new PathProcessor(paramValue, targetType);
    }

}
