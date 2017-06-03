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

package io.servicecomb.swagger.generator.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.servicecomb.swagger.converter.ConverterMgr;
import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.SwaggerConst;
import io.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import io.servicecomb.common.javassist.ClassConfig;
import io.servicecomb.common.javassist.JavassistUtils;

import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public final class ClassUtils {
    private ClassUtils() {
    }

    public static Class<?> getClassByName(ClassLoader classLoader, String clsName) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            return classLoader.loadClass(clsName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // 获取modelImpl对应的class
    public static Class<?> getOrCreateClass(ClassLoader classLoader, String packageName, Swagger swagger,
            ModelImpl modelImpl,
            String clsName) {
        Class<?> cls = getClassByName(classLoader, clsName);
        if (cls != null) {
            return cls;
        }

        ClassConfig classConfig = new ClassConfig();
        classConfig.setClassName(clsName);

        for (Entry<String, Property> entry : modelImpl.getProperties().entrySet()) {
            JavaType propertyJavaType =
                ConverterMgr.findJavaType(classLoader,
                        packageName,
                        swagger,
                        entry.getValue());
            classConfig.addField(entry.getKey(), propertyJavaType);
        }

        cls = JavassistUtils.createClass(classConfig);
        return cls;
    }

    // 将一系列body parameter包装成一个class
    public static Class<?> getOrCreateBodyClass(OperationGenerator operationGenerator,
            List<BodyParameter> bodyParameters) {
        SwaggerGenerator swaggerGenerator = operationGenerator.getSwaggerGenerator();
        Method method = operationGenerator.getProviderMethod();
        String clsName = swaggerGenerator.ensureGetPackageName() + "." + method.getName() + "Body";
        Class<?> cls = getClassByName(swaggerGenerator.getClassLoader(), clsName);
        if (cls != null) {
            return cls;
        }

        ClassConfig classConfig = new ClassConfig();
        classConfig.setClassName(clsName);

        // 1.全是预备body
        // 2.预备body与明确body混合
        for (BodyParameter bp : bodyParameters) {
            JavaType javaType = ConverterMgr.findJavaType(swaggerGenerator.getClassLoader(),
                    swaggerGenerator.ensureGetPackageName(),
                    swaggerGenerator.getSwagger(),
                    bp);
            classConfig.addField(bp.getName(), javaType);
        }

        return JavassistUtils.createClass(classConfig);
    }

    public static boolean hasAnnotation(Class<?> cls, Class<? extends Annotation> annotation) {
        if (cls.getAnnotation(annotation) != null) {
            return true;
        }

        for (Method method : cls.getMethods()) {
            if (method.getAnnotation(annotation) != null) {
                return true;
            }
        }

        return false;
    }

    public static boolean isRawJsonType(Parameter param) {
        Object rawJson = param.getVendorExtensions().get(SwaggerConst.EXT_RAW_JSON_TYPE);
        if (Boolean.class.isInstance(rawJson)) {
            return (boolean) rawJson;
        }
        return false;
    }

    public static Class<?> getJavaInterface(Swagger swagger) {
        return getClassByVendorExtensions(null, swagger.getInfo().getVendorExtensions(), SwaggerConst.EXT_JAVA_INTF);
    }

    public static Class<?> getClassByVendorExtensions(ClassLoader classLoader, Map<String, Object> vendorExtensions,
            String clsKey) {
        if (vendorExtensions == null) {
            return null;
        }

        String clsName = (String) vendorExtensions.get(clsKey);
        if (StringUtils.isEmpty(clsName)) {
            return null;
        }

        return getClassByName(classLoader, clsName);
    }

    public static Class<?> getOrCreateInterface(SwaggerGenerator generator) {
        return getOrCreateInterface(generator.getSwagger(),
                generator.getClassLoader(),
                generator.ensureGetPackageName());
    }

    public static Class<?> getOrCreateInterface(Swagger swagger, ClassLoader classLoader, String packageName) {
        String intfName =
            (String) swagger.getInfo().getVendorExtensions().get(SwaggerConst.EXT_JAVA_INTF);
        Class<?> intf = getClassByName(classLoader, intfName);
        if (intf != null) {
            return intf;
        }

        if (packageName == null) {
            int idx = intfName.lastIndexOf(".");
            if (idx == -1) {
                packageName = "";
            } else {
                packageName = intfName.substring(0, idx);
            }
        }
        return createInterface(swagger, classLoader, packageName, intfName);
    }

    private static Class<?> createInterface(Swagger swagger, ClassLoader classLoader, String packageName,
            String intfName) {
        ClassConfig classConfig = new ClassConfig();
        classConfig.setClassName(intfName);
        classConfig.setIntf(true);

        StringBuilder sbMethod = new StringBuilder();
        StringBuilder sbMethodGenericSignature = new StringBuilder();
        for (Path path : swagger.getPaths().values()) {
            for (Operation operation : path.getOperations()) {
                boolean hasGenericSignature = false;

                sbMethod.setLength(0);
                sbMethodGenericSignature.setLength(0);

                Response result = operation.getResponses().get(SwaggerConst.SUCCESS_KEY);
                JavaType resultJavaType = ConverterMgr.findJavaType(classLoader,
                        packageName,
                        swagger,
                        result.getSchema());
                hasGenericSignature = hasGenericSignature || resultJavaType.hasGenericTypes();

                sbMethod.append(JavassistUtils.getNameForGenerateCode(resultJavaType))
                        .append(" ")
                        .append(operation.getOperationId())
                        .append("(");
                sbMethodGenericSignature.append("(");
                for (Parameter parameter : operation.getParameters()) {
                    String paramName = parameter.getName();
                    paramName = correctMethodParameterName(paramName);
                    JavaType paramJavaType = ConverterMgr.findJavaType(classLoader,
                            packageName,
                            swagger,
                            parameter);
                    hasGenericSignature = hasGenericSignature || paramJavaType.hasGenericTypes();

                    String code = String.format("%s %s,", paramJavaType.getRawClass().getName(), paramName);
                    sbMethod.append(code);
                    sbMethodGenericSignature.append(paramJavaType.getGenericSignature());
                }
                if (!operation.getParameters().isEmpty()) {
                    sbMethod.setLength(sbMethod.length() - 1);
                }
                sbMethod.append(");");
                sbMethodGenericSignature.append(")");
                sbMethodGenericSignature.append(resultJavaType.getGenericSignature());

                if (hasGenericSignature) {
                    classConfig.addMethod(sbMethod.toString(), sbMethodGenericSignature.toString());
                } else {
                    classConfig.addMethod(sbMethod.toString(), null);
                }
            }
        }

        return JavassistUtils.createClass(classLoader, classConfig);
    }

    public static String correctMethodParameterName(String name) {
        return name.replace(".", "_").replace("-", "_");
    }

    public static String correctClassName(String name) {
        return name.replace("-", "_");
    }
}
