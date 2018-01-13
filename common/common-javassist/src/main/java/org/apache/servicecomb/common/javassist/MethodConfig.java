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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JavaType;

public class MethodConfig {
  private String name;

  private JavaType result;

  private List<ParameterConfig> parameterList = new ArrayList<>();

  // 不包括前后的{}
  private String bodySource;

  // 根据上面的信息，生成下面两个字段
  // 包括method声明和body
  // 如果是接口，则只是method声明
  private String source;

  // 泛型声明，如果method参数及应答中没有泛型类型，则此字段应该为null
  private String genericSignature;

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getGenericSignature() {
    return genericSignature;
  }

  public void setGenericSignature(String genericSignature) {
    this.genericSignature = genericSignature;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setResult(JavaType result) {
    this.result = result;
  }

  public void addParameter(String name, JavaType type) {
    ParameterConfig parameterConfig = new ParameterConfig();
    parameterConfig.setName(name);
    parameterConfig.setType(type);
    parameterList.add(parameterConfig);
  }

  public void setBodySource(String bodySource) {
    this.bodySource = bodySource;
  }

  void init() {
    if (source != null) {
      return;
    }

    StringBuilder sbMethod = new StringBuilder();
    StringBuilder sbMethodGenericSignature = new StringBuilder();

    sbMethod.append("public ");
    sbMethod.append(result == null ? "void" : JavassistUtils.getNameForGenerateCode(result));
    sbMethod.append(" ")
        .append(name)
        .append("(");
    sbMethodGenericSignature.append("(");

    boolean hasGenericSignature = result == null ? false : result.hasGenericTypes();
    for (ParameterConfig parameter : parameterList) {
      hasGenericSignature = hasGenericSignature || parameter.getType().hasGenericTypes();

      String paramTypeName = JavassistUtils.getNameForGenerateCode(parameter.getType());
      String code = String.format("%s %s,", paramTypeName, parameter.getName());
      sbMethod.append(code);
      sbMethodGenericSignature.append(parameter.getType().getGenericSignature());
    }

    if (!parameterList.isEmpty()) {
      sbMethod.setLength(sbMethod.length() - 1);
    }
    sbMethod.append(")");
    sbMethodGenericSignature.append(")");
    sbMethodGenericSignature.append(result == null ? "V" : result.getGenericSignature());

    if (bodySource != null) {
      sbMethod.append("{").append(bodySource).append("}");
    } else {
      sbMethod.append(";");
    }

    source = sbMethod.toString();
    if (hasGenericSignature) {
      genericSignature = sbMethodGenericSignature.toString();
    }
  }
}
