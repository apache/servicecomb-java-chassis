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

package io.servicecomb.common.javassist;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public final class JavassistUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavassistUtils.class);

    private static final ClassPool POOL = ClassPool.getDefault();

    // servlet场景下，不添加classpath，则默认是java.lang.Object对应人classpath，找不到web-inf/lib中的jar
    private static final Map<ClassLoader, ClassLoader> CLASSLOADER_SET = new IdentityHashMap<>();

    private static final Object LOCK = new Object();

    private JavassistUtils() {
    }

    private static void appendThreadClassPath() {
        synchronized (LOCK) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (CLASSLOADER_SET.containsKey(classLoader)) {
                return;
            }

            POOL.appendClassPath(new LoaderClassPath(classLoader));
            CLASSLOADER_SET.put(classLoader, classLoader);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Class<? extends Enum> createEnum(String clsName, String... values) {
        return createEnum(null, clsName, Arrays.asList(values));
    }

    @SuppressWarnings("rawtypes")
    public static Class<? extends Enum> createEnum(String clsName, List<String> values) {
        return createEnum(null, clsName, values);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Class<? extends Enum> createEnum(ClassLoader classLoader, String clsName, List<String> values) {
        if (values == null || values.size() == 0) {
            throw new Error("values is not allowed empty.");
        }

        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        CtClass ctClass = POOL.makeClass(clsName);
        ctClass.setModifiers(ctClass.getModifiers() | javassist.Modifier.ENUM);

        try {
            ctClass.setSuperclass(POOL.get(Enum.class.getName()));

            addEnumConstructor(ctClass);
            addEnumValuesMethod(ctClass, values);

            return ctClass.toClass(classLoader, null);
        } catch (Throwable e) {
            throw new Error(e);
        }

    }

    private static void addEnumConstructor(CtClass ctClass) throws Exception {
        String src = "super($1, $2);";
        CtConstructor ctConstructor = new CtConstructor(
                POOL.get(new String[] {String.class.getName(), int.class.getName()}), ctClass);
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

    public static Class<?> createClass(ClassLoader classLoader, ClassConfig config) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        appendThreadClassPath();

        CtClass ctClass = null;
        if (config.isIntf()) {
            ctClass = POOL.makeInterface(config.getClassName());
        } else {

            ctClass = POOL.makeClass(config.getClassName());
        }

        try {
            for (String intfName : config.getIntfList()) {
                ctClass.addInterface(POOL.get(intfName));
            }

            for (FieldConfig fieldConfig : config.getFieldList()) {
                CtField field = createCtField(POOL, ctClass, fieldConfig);
                ctClass.addField(field);
            }

            for (MethodConfig methodConfig : config.getMethodList()) {
                CtMethod ctMethod = CtMethod.make(methodConfig.getSource(), ctClass);
                if (methodConfig.getGenericSignature() != null) {
                    ctMethod.setGenericSignature(methodConfig.getGenericSignature());
                }
                ctClass.addMethod(ctMethod);
            }

            return ctClass.toClass(classLoader, null);
        } catch (Throwable e) {
            throw new Error(e);
        }
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

    private static String genReadFieldsMethodSource(List<FieldConfig> fieldList) throws Exception {
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

    private static String genWriteFieldsMethodSource(List<FieldConfig> fieldList) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("public void writeFields(Object[] values){");
        for (int idx = 0; idx < fieldList.size(); idx++) {
            FieldConfig fieldConfig = fieldList.get(idx);

            String fieldName = fieldConfig.getName();
            Class<?> type = fieldConfig.getType();
            String code = String.format("    %s = (%s)values[%d];",
                    fieldName,
                    type.getTypeName(),
                    idx);

            sb.append(code);
        }
        sb.append("}");

        return sb.toString();
    }

    private static String genReadFieldMethodSource(List<FieldConfig> fieldList) throws Exception {
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

    private static String genWriteFieldMethodSource(List<FieldConfig> fieldList) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("public void writeField(Object value){");

        if (!fieldList.isEmpty()) {
            FieldConfig fieldConfig = fieldList.get(0);
            sb.append(String.format("    %s=(%s)value;", fieldConfig.getName(), fieldConfig.getType().getTypeName()));
        }

        sb.append("}");

        return sb.toString();
    }

    private static CtField createCtField(ClassPool pool, CtClass ctClass, FieldConfig field) throws Exception {
        Class<?> fieldType = field.getType();

        CtField ctField = new CtField(pool.getCtClass(fieldType.getName()), field.getName(), ctClass);
        if (field.getGenericSignature() != null) {
            ctField.setGenericSignature(field.getGenericSignature());
        }
        ctField.setModifiers(Modifier.PUBLIC);
        return ctField;
    }

    public static String getNameForGenerateCode(JavaType javaType) {
        if (byte[].class.equals(javaType.getRawClass())) {
            return "byte[]";
        }

        if (!javaType.isArrayType()) {
            return javaType.getRawClass().getName();
        }

        return javaType.getContentType().getRawClass().getName() + "[]";
    }
}
