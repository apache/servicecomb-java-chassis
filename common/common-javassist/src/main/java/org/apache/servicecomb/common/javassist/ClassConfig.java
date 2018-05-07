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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ClassConfig {

  private String className;

  private boolean intf;

  private List<String> intfList = new ArrayList<>();

  private List<FieldConfig> fieldList = new ArrayList<>();

  private List<MethodConfig> methodList = new ArrayList<>();

  public boolean isIntf() {
    return intf;
  }

  public void setIntf(boolean intf) {
    this.intf = intf;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void addInterface(Class<?> intf) {
    addInterface(intf.getName());
  }

  public void addInterface(String intf) {
    intfList.add(intf);
  }

  public List<String> getIntfList() {
    return intfList;
  }

  public List<FieldConfig> getFieldList() {
    return fieldList;
  }

  public FieldConfig addField(String name, Type genericType) {
    return addField(name, TypeFactory.defaultInstance().constructType(genericType));
  }

  public FieldConfig addField(String name, JavaType javaType) {
    return addField(name, new CtType(javaType));
  }

  public FieldConfig addField(String name, CtType ctType) {
    FieldConfig field = new FieldConfig();
    field.setName(name);
    field.setType(ctType);

    fieldList.add(field);

    return field;
  }

  public void addMethod(MethodConfig methodConfig) {
    methodConfig.init();
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

  public List<MethodConfig> getMethodList() {
    return methodList;
  }
}
