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

package org.apache.servicecomb.swagger.converter;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.common.javassist.ClassConfig;
import org.apache.servicecomb.common.javassist.CtTypeJavaType;
import org.apache.servicecomb.common.javassist.JavassistUtils;
import org.apache.servicecomb.common.javassist.MethodConfig;
import org.apache.servicecomb.swagger.generator.core.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.annotations.VisibleForTesting;

import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import javassist.ClassPool;
import javassist.CtClass;

/**
 * generate interface from swagger<br>
 * specially should support: recursive dependency<br>
 * <pre>
 * 1.class A {
 *     A a;
 *   }
 * 2.class A {
 *     B b;
 *   }
 *   class B {
 *     A a;
 *   }
 * </pre>
 * javassist can create normal dynamic class to classloader<br>
 * but can not create recursive dependency dynamic class to classloader directly<br>
 * to support recursive dependency, must save all class to byte[], and then convert to class<br>
 *
 */
public class SwaggerToClassGenerator {
  private ClassLoader classLoader;

  private Swagger swagger;

  /**
   * package for definitions that no x-java-class attribute
   */
  private String packageName;

  /**
   * if not set, then will get from swagger.info.vendorExtensions.x-java-interface
   * if still not set, then will use ${packageName}.SchemaInterface
   */
  private String interfaceName;

  private Class<?> interfaceCls;

  private TypeFactory typeFactory;

  private ClassPool classPool;

  // key is swagger model or property
  @VisibleForTesting
  protected Map<Object, JavaType> swaggerObjectMap = new IdentityHashMap<>();

  /**
   *
   * @param classLoader
   * @param swagger
   * @param packageName package for definitions that no x-java-class attribute
   */
  public SwaggerToClassGenerator(ClassLoader classLoader, Swagger swagger, String packageName) {
    this.classLoader = classLoader;
    this.swagger = swagger;
    this.packageName = packageName;

    this.typeFactory = TypeFactory.defaultInstance().withClassLoader(classLoader);
    this.classPool = JavassistUtils.getOrCreateClassPool(classLoader);
  }

  public ClassPool getClassPool() {
    return classPool;
  }

  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public String getPackageName() {
    return packageName;
  }

  public TypeFactory getTypeFactory() {
    return typeFactory;
  }

  public Class<?> getInterfaceCls() {
    return interfaceCls;
  }

  /**
   * convert definitions/parameters and responses to java class
   * convert swagger to interface
   */
  public Class<?> convert() {
    collectInterfaceName();
    mapDefinitionsToExistingClasses();
    convertDefinitions();
    convertResponses();
    convertPendingCtClasses();
    convertToInterface();

    return interfaceCls;
  }

  protected void collectInterfaceName() {
    if (interfaceName != null) {
      return;
    }

    if (swagger.getInfo() != null) {
      interfaceName = ClassUtils.getInterfaceName(swagger.getInfo().getVendorExtensions());
      if (interfaceName != null) {
        return;
      }
    }

    interfaceName = packageName + ".SchemaInterface";
  }

  protected void mapDefinitionsToExistingClasses() {
    interfaceCls = ClassUtils.getClassByName(classLoader, interfaceName);
    if (interfaceCls == null) {
      return;
    }

    // TODO: map
  }

  protected void convertDefinitions() {
    if (swagger.getDefinitions() == null) {
      return;
    }

    for (Entry<String, Model> entry : swagger.getDefinitions().entrySet()) {
      convertModel(entry.getKey(), entry.getValue());
    }
  }

  protected void convertResponses() {
    if (swagger.getPaths() == null) {
      return;
    }

    for (Path path : swagger.getPaths().values()) {
      for (Operation operation : path.getOperations()) {
        for (Response response : operation.getResponses().values()) {
          convert(response.getSchema());

          Map<String, Property> headers = response.getHeaders();
          if (headers == null) {
            continue;
          }
          for (Property header : headers.values()) {
            convert(header);
          }
        }
      }
    }
  }

  protected void convertToInterface() {
    if (interfaceCls != null) {
      return;
    }

    ClassConfig classConfig = new ClassConfig();
    classConfig.setClassName(interfaceName);
    classConfig.setIntf(true);

    if (swagger.getPaths() != null) {
      for (Path path : swagger.getPaths().values()) {
        for (Operation operation : path.getOperations()) {
          Response result = operation.getResponses().get(SwaggerConst.SUCCESS_KEY);
          JavaType resultJavaType = swaggerObjectMap.get(result.getSchema());

          MethodConfig methodConfig = new MethodConfig();
          methodConfig.setName(operation.getOperationId());
          methodConfig.setResult(resultJavaType);

          for (Parameter parameter : operation.getParameters()) {
            String paramName = parameter.getName();
            paramName = ClassUtils.correctMethodParameterName(paramName);

            JavaType paramJavaType = ConverterMgr.findJavaType(this, parameter);
            methodConfig.addParameter(paramName, paramJavaType);
          }

          classConfig.addMethod(methodConfig);
        }
      }
    }

    interfaceCls = JavassistUtils.createClass(classLoader, classConfig);
  }

  protected void convertPendingCtClasses() {
    for (Entry<Object, JavaType> entry : swaggerObjectMap.entrySet()) {
      JavaType javaType = entry.getValue();
      if (!CtTypeJavaType.class.isInstance(javaType)) {
        continue;
      }

      CtClass ctClass = ((CtTypeJavaType) javaType).getType().getCtClass();
      Class<?> cls = JavassistUtils.createClass(classLoader, ctClass);
      entry.setValue(typeFactory.constructType(cls));
    }
  }

  public JavaType convert(Object swaggerObject) {
    JavaType javaType = swaggerObjectMap.get(swaggerObject);
    if (javaType == null) {
      javaType = ConverterMgr.findJavaType(this, swaggerObject);
      swaggerObjectMap.put(swaggerObject, javaType);
    }
    return javaType;
  }

  /**
   * just only for invoker know that there is no recursive dependency
   *
   */
  public JavaType forceConvert(Object swaggerObject) {
    convert(swaggerObject);
    convertPendingCtClasses();
    return swaggerObjectMap.get(swaggerObject);
  }

  protected void updateJavaClassInVendor(Map<String, Object> vendorExtensions, String shortClsName) {
    String clsName = ClassUtils.getClassName(vendorExtensions);
    if (StringUtils.isEmpty(clsName)) {
      vendorExtensions.put(SwaggerConst.EXT_JAVA_CLASS, packageName + "." + shortClsName);
    }
  }

  protected JavaType convertModel(String name, Model model) {
    updateJavaClassInVendor(model.getVendorExtensions(), name);
    return convert(model);
  }

  public JavaType convertRef(String ref) {
    return convertModel(ref, swagger.getDefinitions().get(ref));
  }
}
