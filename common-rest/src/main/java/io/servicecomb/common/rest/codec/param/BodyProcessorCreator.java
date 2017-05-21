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

import java.io.InputStream;
import java.lang.reflect.Type;

import io.servicecomb.common.rest.codec.RestClientRequest;
import io.servicecomb.common.rest.codec.RestObjectMapper;
import io.servicecomb.common.rest.codec.RestServerRequest;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.servicecomb.foundation.vertx.stream.BufferOutputStream;

import io.vertx.core.buffer.Buffer;

public class BodyProcessorCreator implements ParamValueProcessorCreator {

    public static final String PARAMTYPE = "body";

    /**
     * Body Parameter Processor
     * @author   
     * @version  [版本号, 2017年1月2日]
     * @see  [相关类/方法]
     * @since  [产品/模块版本]
     */
    public static class BodyProcessor implements ParamValueProcessor {
        protected JavaType targetType;

        public BodyProcessor(JavaType targetType) {
            this.targetType = targetType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(RestServerRequest request) throws Exception {
            // 从payload中获取参数
            Object body = request.getBody();
            if (body == null) {
                return null;
            }

            if (InputStream.class.isInstance(body)) {
                InputStream inputStream = (InputStream) body;
                return RestObjectMapper.INSTANCE.readValue(inputStream, targetType);
            }

            return RestObjectMapper.INSTANCE.convertValue(body, targetType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
            try (BufferOutputStream output = new BufferOutputStream()) {
                RestObjectMapper.INSTANCE.writeValue(output, arg);
                clientRequest.write(output.getBuffer());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getParameterPath() {
            return "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getProcessorType() {
            return PARAMTYPE;
        }
    }

    /**
     * 针对raw json string类型的Body Processor
     * @author   
     * @version  [版本号, 2017年3月10日]
     * @see  [相关类/方法]
     * @since  [产品/模块版本]
     */
    public static class RawJsonBodyProcessor extends BodyProcessor {

        public RawJsonBodyProcessor(JavaType targetType) {
            super(targetType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(RestServerRequest request) throws Exception {
            // 从payload中获取参数
            Object body = request.getBody();
            if (body == null) {
                return null;
            }

            if (InputStream.class.isInstance(body)) {
                InputStream inputStream = (InputStream) body;
                return IOUtils.toString(inputStream);
            }

            return RestObjectMapper.INSTANCE.convertValue(body, targetType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
            clientRequest.write(Buffer.buffer((String) arg));
        }

    }

    public BodyProcessorCreator() {
        ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParamValueProcessor create(String paramValue, Type genericParamType) {
        JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
        return new BodyProcessor(targetType);
    }

    public ParamValueProcessor create(boolean rawJson, Type genericParamType) {
        JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
        if (genericParamType.getTypeName().equals(String.class.getTypeName()) && rawJson) {
            return new RawJsonBodyProcessor(targetType);
        }
        return new BodyProcessor(targetType);
    }

}
