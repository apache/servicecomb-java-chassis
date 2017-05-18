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

package com.huawei.paas.cse.codec.protobuf.codec;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;
import com.huawei.paas.cse.codec.protobuf.definition.ProtobufManager;

/**
 * protobuf标准场景，一个schema对应一个msg，对应一个java实例
 * @author   
 * @version  [版本号, 2016年12月6日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class StandardResultCodec extends AbstractCodec {
    @Override
    public void init(ProtobufSchema schema, Type... types) {
        writer = ProtobufManager.getMapper().writer(schema);
        reader = ProtobufManager.getMapper().reader(schema);

        // 需要考虑void场景
        if (types.length == 1) {
            JavaType javaType = TypeFactory.defaultInstance().constructType(types[0]);
            writer = writer.forType(javaType);
            reader = reader.forType(javaType);
        }
    }
}
