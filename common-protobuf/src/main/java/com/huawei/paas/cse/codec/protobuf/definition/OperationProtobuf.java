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

package com.huawei.paas.cse.codec.protobuf.definition;

import java.lang.reflect.Method;

import javax.ws.rs.core.Response.Status.Family;

import com.huawei.paas.cse.codec.protobuf.utils.ProtobufSchemaUtils;
import com.huawei.paas.cse.codec.protobuf.utils.WrapSchema;
import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.swagger.invocation.response.ResponseMeta;

public class OperationProtobuf {
    private OperationMeta operationMeta;

    private WrapSchema requestSchema;

    private WrapSchema responseSchema;

    public OperationProtobuf(OperationMeta operationMeta)
        throws Exception {
        this.operationMeta = operationMeta;

        requestSchema = ProtobufSchemaUtils.getOrCreateArgsSchema(operationMeta);

        Method method = operationMeta.getMethod();
        responseSchema = ProtobufSchemaUtils.getOrCreateSchema(method.getReturnType(), method.getGenericReturnType());
    }

    /**
     * 获取operationMeta的值
     * @return 返回 operationMeta
     */
    public OperationMeta getOperationMeta() {
        return operationMeta;
    }

    /**
     * 获取requestSchema的值
     * @return 返回 requestSchema
     */
    public WrapSchema getRequestSchema() {
        return requestSchema;
    }

    /**
     * 获取responseSchema的值
     * @return 返回 responseSchema
     */
    public WrapSchema getResponseSchema() {
        return responseSchema;
    }

    public WrapSchema findResponseSchema(int statusCode) {
        if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
            return responseSchema;
        }

        ResponseMeta responseMeta = operationMeta.findResponseMeta(statusCode);
        return ProtobufSchemaUtils.getOrCreateSchema(responseMeta.getJavaType());
    }
}
