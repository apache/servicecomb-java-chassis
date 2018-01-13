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

package org.apache.servicecomb.swagger.generator.core;

import java.lang.annotation.Annotation;

import org.apache.servicecomb.foundation.common.RegisterManager;

public class AnnotationProcessorManager<T> extends RegisterManager<String, T> {
  enum AnnotationType {
    CLASS,
    METHOD,
    PARAMETER
  }

  public AnnotationProcessorManager(AnnotationType annotationType) {
    super(annotationType + " annotation processor mgr");
  }

  public void register(Class<? extends Annotation> annotationCls, T processor) {
    register(annotationCls.getName(), processor);
  }

  public T findProcessor(Class<? extends Annotation> annotationCls) {
    return this.findValue(annotationCls.getName());
  }

  public T findProcessor(String annotationName) {
    return this.findValue(annotationName);
  }
}
