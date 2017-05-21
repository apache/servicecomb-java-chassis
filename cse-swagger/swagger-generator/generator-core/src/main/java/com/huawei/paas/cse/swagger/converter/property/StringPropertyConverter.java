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

package com.huawei.paas.cse.swagger.converter.property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.servicecomb.common.javassist.JavassistUtils;
import com.huawei.paas.cse.swagger.converter.ConverterMgr;

import io.swagger.models.Swagger;
import io.swagger.models.properties.StringProperty;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author
 * @version  [版本号, 2017年3月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class StringPropertyConverter extends AbstractPropertyConverter {
    // 用于生成唯一的enum名称
    // key为enum names， value为enum cls javaType
    private static Map<String, JavaType> enumMap = new HashMap<>();

    private static final Object LOCK = new Object();

    // 转换并创建enum是小概率事件，没必要double check
    private static JavaType getOrCreateEnumByNames(String packageName, List<String> enums) {
        String strEnums = enums.toString();

        synchronized (LOCK) {
            JavaType javaType = enumMap.get(strEnums);
            if (javaType != null) {
                return javaType;
            }

            String enumClsName = packageName + ".Enum" + enumMap.size();
            @SuppressWarnings("rawtypes")
            Class<? extends Enum> enumCls = JavassistUtils.createEnum(enumClsName, enums);
            javaType = TypeFactory.defaultInstance().constructType(enumCls);
            enumMap.put(strEnums, javaType);

            return javaType;
        }
    }

    public static JavaType findJavaType(ClassLoader classLoader, String packageName, Swagger swagger, String type,
            String format, List<String> enums) {
        if (enums == null || enums.isEmpty()) {
            return ConverterMgr.findJavaType(type, format);
        }

        // enum，且需要动态生成class
        return getOrCreateEnumByNames(packageName, enums);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object property) {
        StringProperty stringProperty = (StringProperty) property;
        List<String> enums = stringProperty.getEnum();
        return findJavaType(classLoader,
                packageName,
                swagger,
                stringProperty.getType(),
                stringProperty.getFormat(),
                enums);
    }

}
