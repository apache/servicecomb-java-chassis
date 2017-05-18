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

package com.huawei.paas.cse.common.rest.codec.param;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.huawei.paas.cse.common.rest.codec.RestClientRequest;
import com.huawei.paas.cse.common.rest.codec.RestServerRequest;

/**
 * 用于处理Jaxrs中的Form类型参数
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class FormProcessorCreator implements ParamValueProcessorCreator {
    public static final String PARAMTYPE = "formData";

    public static class FormProcessor extends AbstractParamProcessor {
        public FormProcessor(String paramPath, JavaType targetType) {
            super(paramPath, targetType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(RestServerRequest request) throws Exception {
            Object param = request.getFormParam(paramPath);
            if (param == null) {
                return null;
            }

            return convertValue(param, targetType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
            clientRequest.addForm(paramPath, arg);
        }

        @Override
        public String getProcessorType() {
            return PARAMTYPE;
        }
    }

    public FormProcessorCreator() {
        ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParamValueProcessor create(String paramValue, Type genericParamType) {
        JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);
        return new FormProcessor(paramValue, targetType);
    }
}
