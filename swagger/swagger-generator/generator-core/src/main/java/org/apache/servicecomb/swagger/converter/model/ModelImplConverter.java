/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.swagger.converter.model;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.common.javassist.ClassConfig;
import org.apache.servicecomb.common.javassist.CtType;
import org.apache.servicecomb.common.javassist.CtTypeJavaType;
import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.converter.SwaggerToClassGenerator;
import org.apache.servicecomb.swagger.converter.property.MapPropertyConverter;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import javassist.CtClass;

public class ModelImplConverter extends AbstractModelConverter {
  @Override
  public JavaType doConvert(SwaggerToClassGenerator swaggerToClassGenerator, Object model) {
    ModelImpl modelImpl = (ModelImpl) model;

    JavaType javaType = ConverterMgr.findJavaType(modelImpl.getType(), modelImpl.getFormat());
    if (javaType != null) {
      return javaType;
    }

    if (modelImpl.getReference() != null) {
      return swaggerToClassGenerator.convertRef(modelImpl.getReference());
    }

    if (modelImpl.getAdditionalProperties() != null) {
      return MapPropertyConverter.findJavaType(swaggerToClassGenerator, modelImpl.getAdditionalProperties());
    }

    if (ObjectProperty.TYPE.equals(modelImpl.getType())
        && modelImpl.getProperties() == null
        && modelImpl.getName() == null) {
      return TypeFactory.defaultInstance().constructType(Object.class);
    }

    return getOrCreateType(swaggerToClassGenerator, modelImpl);
  }

  protected JavaType getOrCreateType(SwaggerToClassGenerator swaggerToClassGenerator, ModelImpl modelImpl) {
    String clsName = ClassUtils.getClassName(findVendorExtensions(modelImpl));
    clsName = ClassUtils.correctClassName(clsName);

    return getOrCreateType(swaggerToClassGenerator, modelImpl.getProperties(), clsName);
  }

  protected JavaType getOrCreateType(SwaggerToClassGenerator swaggerToClassGenerator,
      Map<String, Property> properties,
      String clsName) {
    Class<?> cls = ClassUtils.getClassByName(swaggerToClassGenerator.getClassLoader(), clsName);
    if (cls != null) {
      return swaggerToClassGenerator.getTypeFactory().constructType(cls);
    }

    CtClass ctClass = getOrCreateCtClass(swaggerToClassGenerator, properties, clsName);
    return new CtTypeJavaType(new CtType(ctClass));
  }

  private CtClass getOrCreateCtClass(SwaggerToClassGenerator swaggerToClassGenerator, Map<String, Property> properties,
      String clsName) {
    CtClass ctClass = swaggerToClassGenerator.getClassPool().getOrNull(clsName);
    if (ctClass != null) {
      return ctClass;
    }

    // must ensure already create CtClass, otherwise recursive dependency class will create failed.
    swaggerToClassGenerator.getClassPool().makeClass(clsName);

    ClassConfig classConfig = new ClassConfig();
    classConfig.setClassName(clsName);

    if (null != properties) {
      for (Entry<String, Property> entry : properties.entrySet()) {
        JavaType propertyJavaType = swaggerToClassGenerator.convert(entry.getValue());
        classConfig.addField(entry.getKey(), propertyJavaType);
      }
    }

    return JavassistUtils.createCtClass(swaggerToClassGenerator.getClassLoader(), classConfig);
  }
}
