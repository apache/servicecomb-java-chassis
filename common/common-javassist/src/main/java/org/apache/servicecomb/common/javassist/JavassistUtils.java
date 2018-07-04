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

package org.apache.servicecomb.common.javassist;

import static java.util.Locale.ENGLISH;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;

public final class JavassistUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(JavassistUtils.class);

  static {
    ScopedClassPoolRepositoryImpl.getInstance().setClassPoolFactory(new StdScopedClassPoolFactory());
  }

  public static ClassPool getOrCreateClassPool(ClassLoader classLoader) {
    return ScopedClassPoolRepositoryImpl.getInstance().registerClassLoader(classLoader);
  }

  public static void clearByClassLoader(ClassLoader classLoader) {
    ScopedClassPoolRepositoryImpl.getInstance().unregisterClassLoader(classLoader);
  }

  private JavassistUtils() {
  }

  @SuppressWarnings("rawtypes")
  public static Class<? extends Enum> createEnum(String clsName, String... values) {
    return createEnum(null, clsName, Arrays.asList(values));
  }

  @SuppressWarnings("rawtypes")
  public static Class<? extends Enum> createEnum(String clsName, List<String> values) {
    return createEnum(null, clsName, values);
  }

  @SuppressWarnings("rawtypes")
  public static Class<? extends Enum> getOrCreateEnumWithPackageName(ClassLoader classLoader, String packageName,
      List<String> enums) {
    String strEnums = enums.toString();
    String enumClsName =
        packageName + ".Enum_" + Hashing.sha256().hashString(strEnums, StandardCharsets.UTF_8).toString();
    return JavassistUtils.getOrCreateEnumWithClassName(classLoader, enumClsName, enums);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static synchronized Class<? extends Enum> getOrCreateEnumWithClassName(ClassLoader classLoader, String clsName,
      List<String> values) {
    try {
      return (Class<? extends Enum>) classLoader.loadClass(clsName);
    } catch (ClassNotFoundException e) {
      return createEnum(classLoader, clsName, values);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static Class<? extends Enum> createEnum(ClassLoader classLoader, String clsName, List<String> values) {
    if (values == null || values.size() == 0) {
      throw new Error("values is not allowed empty.");
    }

    classLoader = JvmUtils.correctClassLoader(classLoader);

    ClassPool classPool = getOrCreateClassPool(classLoader);
    CtClass ctClass = classPool.makeClass(clsName);
    ctClass.setModifiers(ctClass.getModifiers() | javassist.Modifier.ENUM);

    try {
      ctClass.setSuperclass(classPool.get(Enum.class.getName()));

      addEnumConstructor(classPool, ctClass);
      addEnumValuesMethod(ctClass, values);

      return ctClass.toClass(classLoader, null);
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  private static void addEnumConstructor(ClassPool classPool, CtClass ctClass) throws Exception {
    String src = "super($1, $2);";
    CtConstructor ctConstructor = new CtConstructor(
        classPool.get(new String[] {String.class.getName(), int.class.getName()}), ctClass);
    ctConstructor.setBody(src);

    ctClass.addConstructor(ctConstructor);
  }

  private static void addEnumValuesMethod(CtClass ctClass, List<String> values) throws CannotCompileException {
    StringBuilder sb = new StringBuilder();
    sb.append("public static Enum[] values(){return new Enum[]{");
    for (int idx = 0; idx < values.size(); idx++) {
      String value = values.get(idx);

      String line = String.format("new %s(\"%s\", %d),", ctClass.getName(), value, idx);
      sb.append(line);
    }
    sb.setLength(sb.length() - 1);
    sb.append("};}");

    CtMethod valuesMethod = CtMethod.make(sb.toString(), ctClass);
    ctClass.addMethod(valuesMethod);
  }

  public static Class<?> createClass(ClassConfig config) {
    return createClass(null, config);
  }

  public static CtClass createCtClass(ClassLoader classLoader, ClassConfig config) {
    classLoader = JvmUtils.correctClassLoader(classLoader);

    ClassPool classPool = getOrCreateClassPool(classLoader);
    CtClass ctClass = classPool.getOrNull(config.getClassName());
    if (ctClass == null) {
      if (config.isIntf()) {
        ctClass = classPool.makeInterface(config.getClassName());
      } else {
        ctClass = classPool.makeClass(config.getClassName());
      }
    }

    try {
      for (String intfName : config.getIntfList()) {
        ctClass.addInterface(classPool.get(intfName));
      }

      for (FieldConfig fieldConfig : config.getFieldList()) {
        CtField field = createCtField(ctClass, fieldConfig);
        ctClass.addField(field);

        if (fieldConfig.isGenGetter()) {
          addFieldGetter(config, fieldConfig);
        }

        if (fieldConfig.isGenSetter()) {
          addFieldSetter(config, fieldConfig);
        }
      }

      for (MethodConfig methodConfig : config.getMethodList()) {
        try {
          CtMethod ctMethod = CtMethod.make(methodConfig.getSource(), ctClass);
          if (methodConfig.getGenericSignature() != null) {
            ctMethod.setGenericSignature(methodConfig.getGenericSignature());
          }
          ctClass.addMethod(ctMethod);
        } catch (CannotCompileException e) {
          LOGGER.error("Failed to create method, source:\n{}.", methodConfig.getSource());
          throw e;
        }
      }

      LOGGER.info("create CtClass {} in classLoader {}.", config.getClassName(), classLoader);
      return ctClass;
    } catch (Throwable e) {
      throw new IllegalStateException(
          String.format("Failed to create CtClass %s in classLoader %s.", config.getClassName(),
              classLoader), e);
    }
  }

  public static Class<?> createClass(ClassLoader classLoader, ClassConfig config) {
    classLoader = JvmUtils.correctClassLoader(classLoader);

    CtClass ctClass = createCtClass(classLoader, config);
    return createClass(classLoader, ctClass);
  }

  public static Class<?> createClass(ClassLoader classLoader, CtClass ctClass) {
    classLoader = JvmUtils.correctClassLoader(classLoader);

    String clsName = ctClass.getName();
    try {
      // must try load from classloader first
      // because class A depend on class B
      // when load class A, will load class B
      // after this, if CtClass B invoke toClass again, will cause problem.
      return classLoader.loadClass(clsName);
    } catch (ClassNotFoundException e) {
      // ignore it
    }

    try {
      Class<?> cls = ctClass.toClass(classLoader, null);
      LOGGER.info("create class {} in classLoader {}.", clsName, classLoader);
      return cls;
    } catch (Throwable e) {
      throw new IllegalStateException(
          String.format("Failed to create %s in classLoader %s.", clsName, classLoader), e);
    }
  }

  public static String capitalize(String name) {
    if (name == null || name.length() == 0) {
      return name;
    }
    return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
  }

  private static void addFieldGetter(ClassConfig config, FieldConfig fieldConfig) {
    MethodConfig methodConfig = new MethodConfig();

    String prefix = "get";
    if (boolean.class.getName().equals(fieldConfig.getType().getCtClass().getName())
        || Boolean.class.getName().equals(fieldConfig.getType().getCtClass().getName())) {
      prefix = "is";
    }
    methodConfig.setName(prefix + capitalize(fieldConfig.getName()));
    methodConfig.setResult(fieldConfig.getType());
    methodConfig.setBodySource("return " + fieldConfig.getName() + ";");

    config.addMethod(methodConfig);
  }

  private static void addFieldSetter(ClassConfig config, FieldConfig fieldConfig) {
    MethodConfig methodConfig = new MethodConfig();
    methodConfig.setName("set" + capitalize(fieldConfig.getName()));
    methodConfig.addParameter(fieldConfig.getName(), fieldConfig.getType());
    methodConfig.setBodySource(" this." + fieldConfig.getName() + " = " + fieldConfig.getName() + ";");

    config.addMethod(methodConfig);
  }

  public static void genMultiWrapperInterface(ClassConfig config) {
    try {
      config.addInterface(MultiWrapper.class);

      config.addMethod(genReadFieldsMethodSource(config.getFieldList()));
      config.addMethod(genWriteFieldsMethodSource(config.getFieldList()));
    } catch (Exception e) {
      String msg = String.format("failed to genMultiWrapperInterface, name=%s", config.getClassName());
      LOGGER.error(msg, e);

      throw new Error(msg, e);
    }
  }

  public static void genSingleWrapperInterface(ClassConfig config) {
    try {
      config.addInterface(SingleWrapper.class);

      config.addMethod(genReadFieldMethodSource(config.getFieldList()));
      config.addMethod(genWriteFieldMethodSource(config.getFieldList()));
    } catch (Exception e) {
      String msg = String.format("failed to genSingleWrapperMethod, name=%s", config.getClassName());
      LOGGER.error(msg, e);

      throw new Error(msg, e);
    }
  }

  private static String genReadFieldsMethodSource(List<FieldConfig> fieldList) {
    StringBuilder sb = new StringBuilder();
    sb.append("public Object[] readFields(){");
    sb.append(String.format("Object values[] = new Object[%d];", fieldList.size()));

    for (int idx = 0; idx < fieldList.size(); idx++) {
      String fieldName = fieldList.get(idx).getName();
      String code = String.format("    values[%d] = %s;",
          idx,
          fieldName);

      sb.append(code);
    }
    sb.append("return values;");
    sb.append("}");

    return sb.toString();
  }

  private static String genWriteFieldsMethodSource(List<FieldConfig> fieldList) {
    StringBuilder sb = new StringBuilder();
    sb.append("public void writeFields(Object[] values){");
    for (int idx = 0; idx < fieldList.size(); idx++) {
      FieldConfig fieldConfig = fieldList.get(idx);

      String fieldName = fieldConfig.getName();
      String code = String.format("    %s = (%s)values[%d];",
          fieldName,
          fieldConfig.getType().getCtClass().getName(),
          idx);

      sb.append(code);
    }
    sb.append("}");

    return sb.toString();
  }

  private static String genReadFieldMethodSource(List<FieldConfig> fieldList) {
    StringBuilder sb = new StringBuilder();
    sb.append("public Object readField(){");

    String fieldName = "null";
    if (!fieldList.isEmpty()) {
      fieldName = fieldList.get(0).getName();
    }

    sb.append(String.format("    return %s;", fieldName));
    sb.append("}");

    return sb.toString();
  }

  private static String genWriteFieldMethodSource(List<FieldConfig> fieldList) {
    StringBuilder sb = new StringBuilder();
    sb.append("public void writeField(Object value){");

    if (!fieldList.isEmpty()) {
      FieldConfig fieldConfig = fieldList.get(0);
      sb.append(
          String.format("    %s=(%s)value;",
              fieldConfig.getName(),
              fieldConfig.getType().getCtClass().getName()));
    }

    sb.append("}");

    return sb.toString();
  }

  private static CtField createCtField(CtClass ctClass, FieldConfig field) throws CannotCompileException {
    CtField ctField = new CtField(field.getType().getCtClass(), field.getName(), ctClass);
    if (field.getType().hasGenericTypes()) {
      ctField.setGenericSignature(field.getType().getGenericSignature());
    }
    ctField.setModifiers(Modifier.PUBLIC);
    return ctField;
  }
}
