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

package org.apache.servicecomb.swagger.generator.core.unittest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;

public class SwaggerGeneratorForTest extends SwaggerGenerator {
  // 可用于控制一次扫描中有哪些method需要处理
  // 如果methodNameSet为null，表示全部处理
  private Set<String> methodNameSet;

  public SwaggerGeneratorForTest(SwaggerGeneratorContext context, Class<?> cls) {
    super(context, cls);
    setPackageName("gen.cse.ms.ut");
  }

  public boolean containsMethod(String methodName) {
    if (methodNameSet == null) {
      // 无约束
      return true;
    }

    return methodNameSet.contains(methodName);
  }

  protected void clearMethod() {
    if (methodNameSet != null) {
      methodNameSet.clear();
    }
    methodNameSet = null;
  }

  public void replaceMethods(String... methodNames) {
    clearMethod();

    if (methodNames == null || methodNames.length == 0) {
      return;
    }

    if (methodNameSet == null) {
      methodNameSet = new HashSet<>();
    }

    methodNameSet.addAll(Arrays.asList(methodNames));
  }

  @Override
  protected boolean isSkipMethod(Method method) {
    boolean skip = super.isSkipMethod(method);
    if (skip) {
      return true;
    }

    return !containsMethod(method.getName());
  }
}
