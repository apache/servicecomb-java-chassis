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

package com.huawei.paas.cse.common.javassist;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月18日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ClassConfig {

    private String className;

    private boolean intf;

    private List<String> intfList = new ArrayList<>();

    private List<FieldConfig> fieldList = new ArrayList<>();

    private List<MethodConfig> methodList = new ArrayList<>();

    /**
     * 获取intf的值
     * @return 返回 intf
     */
    public boolean isIntf() {
        return intf;
    }

    /**
     * 对intf进行赋值
     * @param intf intf的新值
     */
    public void setIntf(boolean intf) {
        this.intf = intf;
    }

    /**
     * 获取className的值
     * @return 返回 className
     */
    public String getClassName() {
        return className;
    }

    /**
     * 对className进行赋值
     * @param className className的新值
     */
    public void setClassName(String className) {
        this.className = className;
    }

    public void addInterface(Class<?> intf) {
        addInterface(intf.getName());
    }

    public void addInterface(String intf) {
        intfList.add(intf);
    }

    /**
     * 获取intfList的值
     * @return 返回 intfList
     */
    public List<String> getIntfList() {
        return intfList;
    }

    /**
     * 获取fieldList的值
     * @return 返回 fieldList
     */
    public List<FieldConfig> getFieldList() {
        return fieldList;
    }

    public void addField(String name, Class<?> type) {
        addField(name, type, (String) null);
    }

    public void addField(String name, Type genericType) {
        addField(name, TypeFactory.defaultInstance().constructType(genericType));
    }

    public void addField(String name, JavaType javaType) {
        String genericSignature = javaType.hasGenericTypes() ? javaType.getGenericSignature() : null;
        addField(name, javaType.getRawClass(), genericSignature);
    }

    public void addField(String name, Class<?> type, String genericSignature) {
        type = ClassUtils.resolvePrimitiveIfNecessary(type);

        FieldConfig field = new FieldConfig();
        field.setName(name);
        field.setType(type);
        field.setGenericSignature(genericSignature);

        fieldList.add(field);
    }

    public void addMethod(MethodConfig methodConfig) {
        methodList.add(methodConfig);
    }

    public void addMethod(String source) {
        addMethod(source, null);
    }

    public void addMethod(String source, String genericSignature) {
        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setSource(source);
        methodConfig.setGenericSignature(genericSignature);
        addMethod(methodConfig);
    }

    /**
     * 获取methodSourceList的值
     * @return 返回 methodSourceList
     */
    public List<MethodConfig> getMethodList() {
        return methodList;
    }
}
