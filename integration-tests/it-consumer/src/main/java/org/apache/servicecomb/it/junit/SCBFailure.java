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
package org.apache.servicecomb.it.junit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class SCBFailure {

  private final List<String> parents;

  private final String displayName;

  private final Throwable exception;

  public SCBFailure(String displayName, Throwable thrownException) {
    this.displayName = displayName;
    exception = thrownException;

    parents = ITJUnitUtils.cloneParents();
  }

  public List<String> getParents() {
    return parents;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getStacktrace() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    exception.printStackTrace(writer);
    return stringWriter.toString();
  }

  @Override
  public String toString() {
    return "" + displayName + ":" + exception.getMessage();
  }
}
