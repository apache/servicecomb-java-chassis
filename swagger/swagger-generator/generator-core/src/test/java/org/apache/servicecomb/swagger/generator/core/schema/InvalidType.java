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
package org.apache.servicecomb.swagger.generator.core.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvalidType {
  public static interface InvalidIntf {

  }

  public static abstract class InvalidClass {

  }

  public static class InvalidFieldClass {
    public Object obj;
  }

  public InvalidIntf testIntf(InvalidIntf input) {
    return null;
  }

  public InvalidClass testAbstractClass(InvalidClass input) {
    return null;
  }

  public Object testObject() {
    return null;
  }

  @SuppressWarnings("rawtypes")
  public List testNotClearList() {
    return null;
  }

  @SuppressWarnings("rawtypes")
  public Set testNotClearSet() {
    return null;
  }

  public Map<String, Object> testNotClearMap() {
    return null;
  }

  public InvalidFieldClass testInvalidFieldClass() {
    return null;
  }
}
