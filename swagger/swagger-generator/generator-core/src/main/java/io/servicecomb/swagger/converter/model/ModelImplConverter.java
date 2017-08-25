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

package io.servicecomb.swagger.converter.model;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.swagger.converter.ConverterMgr;
import io.servicecomb.swagger.converter.property.MapPropertyConverter;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;

public class ModelImplConverter extends AbstractModelConverter {
  @Override
  public JavaType doConvert(ClassLoader classLoader, String packageName, Swagger swagger, Object model) {
    ModelImpl modelImpl = (ModelImpl) model;

    JavaType javaType = ConverterMgr.findJavaType(modelImpl.getType(), modelImpl.getFormat());
    if (javaType != null) {
      return javaType;
    }

    if (modelImpl.getReference() != null) {
      return ConverterMgr.findByRef(classLoader, packageName, swagger, modelImpl.getReference());
    }

    if (modelImpl.getAdditionalProperties() != null) {
      return MapPropertyConverter.findJavaType(classLoader,
          packageName,
          swagger,
          modelImpl.getAdditionalProperties());
    }

    // 根据name、property动态生成class
    if (packageName == null) {
      throw new Error("packageName should not be null");
    }
    String clsName = packageName + "." + modelImpl.getName();
    Class<?> cls =
        ClassUtils.getOrCreateClass(classLoader, packageName, swagger, modelImpl.getProperties(), clsName);
    return TypeFactory.defaultInstance().constructType(cls);
  }
}
