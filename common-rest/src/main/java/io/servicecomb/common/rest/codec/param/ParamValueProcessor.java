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

import com.fasterxml.jackson.databind.JavaType;

import io.servicecomb.common.rest.codec.RestClientRequest;
import io.servicecomb.common.rest.codec.RestObjectMapper;
import io.servicecomb.common.rest.codec.RestServerRequest;

/**
 * 要求实现者无状态
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface ParamValueProcessor {
    /**
     * 从http request中获取到parameter值
     * @param request
     * @return
     * @throws Exception
     */
    Object getValue(RestServerRequest request) throws Exception;

    /**
     * 将参数值设置到request的相应位置中
     * @param paramData
     * @param arg
     * @throws Exception
     */
    void setValue(RestClientRequest clientRequest, Object arg) throws Exception;

    /**
     * 将对象转换成指定类型的值
     * @param value
     * @param targetType
     * @return
     */
    default Object convertValue(Object value, JavaType targetType) {
        return RestObjectMapper.INSTANCE.convertValue(value, targetType);
    }

    /**
     * 获取该参数的路径名
     * @return
     */
    String getParameterPath();

    /**
     * 获取processor的类型
     * @return
     */
    String getProcessorType();

}
