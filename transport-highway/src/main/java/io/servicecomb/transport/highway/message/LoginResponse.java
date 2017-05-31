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
package io.servicecomb.transport.highway.message;

import io.servicecomb.codec.protobuf.utils.ProtobufSchemaUtils;
import io.servicecomb.codec.protobuf.utils.WrapSchema;

import io.protostuff.ProtobufOutput;
import io.protostuff.Tag;
import io.vertx.core.buffer.Buffer;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @version  [版本号, 2017年5月8日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class LoginResponse {
    private static WrapSchema loginResponseSchema = ProtobufSchemaUtils.getOrCreateSchema(LoginResponse.class);

    public static WrapSchema getLoginResponseSchema() {
        return loginResponseSchema;
    }

    public static LoginResponse readObject(Buffer bodyBuffer) throws Exception {
        return loginResponseSchema.readObject(bodyBuffer);
    }

    @Tag(1)
    private String protocol;

    // 压缩算法名字
    @Tag(2)
    private String zipName;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getZipName() {
        return zipName;
    }

    public void setZipName(String zipName) {
        this.zipName = zipName;
    }

    public void writeObject(ProtobufOutput output) throws Exception {
        loginResponseSchema.writeObject(output, this);
    }
}
