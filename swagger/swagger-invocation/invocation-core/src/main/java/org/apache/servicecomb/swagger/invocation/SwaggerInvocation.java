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

package org.apache.servicecomb.swagger.invocation;

import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;

public class SwaggerInvocation extends InvocationContext {
  // 本实例是在consumer端，还是在provider端
  protected InvocationType invocationType;

  protected InvocationContext parentContext;

  public SwaggerInvocation() {
    parentContext = ContextUtils.getInvocationContext();
    if (parentContext != null) {
      addContext(parentContext.getContext());
      addLocalContext(parentContext.getLocalContext());
    }
  }

  public InvocationContext getParentContext() {
    return parentContext;
  }

  public InvocationType getInvocationType() {
    return invocationType;
  }

  public String getInvocationQualifiedName() {
    return invocationType.name();
  }

  public void onBusinessMethodStart() {
  }

  public void onBusinessMethodFinish() {
  }

  public void onBusinessFinish() {
  }
}
