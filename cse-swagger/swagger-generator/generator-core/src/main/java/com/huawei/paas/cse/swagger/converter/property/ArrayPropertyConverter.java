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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.huawei.paas.cse.swagger.converter.ConverterMgr;

import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author
 * @version  [版本号, 2017年3月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ArrayPropertyConverter extends AbstractPropertyConverter {
    public static JavaType findJavaType(ClassLoader classLoader, String packageName, Swagger swagger,
            Property itemProperty,
            Boolean uniqueItems) {
        JavaType itemJavaType = ConverterMgr.findJavaType(classLoader, packageName, swagger, itemProperty);

        @SuppressWarnings("rawtypes")
        Class<? extends Collection> collectionClass = List.class;
        if (Boolean.TRUE.equals(uniqueItems)) {
            collectionClass = Set.class;
        }
        return TypeFactory.defaultInstance().constructCollectionType(collectionClass, itemJavaType);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object property) {
        ArrayProperty arrayProperty = (ArrayProperty) property;

        return findJavaType(classLoader,
                packageName,
                swagger,
                arrayProperty.getItems(),
                arrayProperty.getUniqueItems());
    }
}
