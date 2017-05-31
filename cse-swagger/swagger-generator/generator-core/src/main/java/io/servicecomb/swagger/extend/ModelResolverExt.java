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

package io.servicecomb.swagger.extend;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import io.servicecomb.swagger.extend.property.ByteProperty;
import io.servicecomb.swagger.extend.property.ShortProperty;

import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @version  [版本号, 2017年3月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ModelResolverExt extends ModelResolver {
    private interface PropertyCreator {
        Property createProperty();
    }

    private Map<Class<?>, PropertyCreator> creatorMap = new HashMap<>();

    /**
     * <构造函数>
     */
    public ModelResolverExt() {
        super(Json.mapper());

        addCreator(() -> {
            return new ByteProperty();
        }, Byte.class, byte.class);

        addCreator(() -> {
            return new ShortProperty();
        }, Short.class, short.class);

        addCreator(() -> {
            return new ByteArrayProperty();
        }, Byte[].class, byte[].class);
    }

    private void addCreator(PropertyCreator creator, Class<?>... clsArr) {
        for (Class<?> cls : clsArr) {
            creatorMap.put(cls, creator);
        }
    }

    private void setType(JavaType type, Map<String, Object> vendorExtensions) {
        vendorExtensions.put(ExtendConst.EXT_JAVA_CLASS, type.getRawClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model resolve(JavaType type, ModelConverterContext context, Iterator<ModelConverter> next) {
        Model model = super.resolve(type, context, next);
        if (model == null) {
            return null;
        }

        // 只有声明model的地方才需要标注类型
        if (ModelImpl.class.isInstance(model) && !StringUtils.isEmpty(((ModelImpl) model).getName())) {
            setType(type, model.getVendorExtensions());
        }
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property resolveProperty(JavaType propType, ModelConverterContext context, Annotation[] annotations,
            Iterator<ModelConverter> next) {
        PropertyCreator creator = creatorMap.get(propType.getRawClass());
        if (creator != null) {
            return creator.createProperty();
        }

        Property property = super.resolveProperty(propType, context, annotations, next);
        //            setType(propType, property.getVendorExtensions());
        return property;
    }
}
