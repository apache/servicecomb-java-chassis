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

import com.fasterxml.jackson.databind.type.SimpleType;

/**
 * just a wrapper for CtType
 * pending create class from CtClass to support recursive dependency class
 */
public class CtTypeJavaType extends SimpleType {
  private static final long serialVersionUID = 301147079248607138L;

  private CtType type;

  public CtTypeJavaType(CtType type) {
    super(CtTypeJavaType.class);
    this.type = type;
  }

  public CtType getType() {
    return type;
  }

  @Override
  protected String buildCanonicalName() {
    return type.getCtClass().getName();
  }

  @Override
  public String getGenericSignature() {
    return type.getGenericSignature();
  }


  @Override
  public StringBuilder getGenericSignature(StringBuilder sb) {
    return sb.append(type.getGenericSignature());
  }
}
