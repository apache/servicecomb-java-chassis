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

package org.apache.servicecomb.core.handler.config;

import org.apache.servicecomb.core.Handler;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class HandlerConfig {
  private String handlerId;

  private Class<Handler> clazz;

  @JacksonXmlProperty(localName = "id", isAttribute = true)
  public String getHandlerId() {
    return handlerId;
  }

  public void setHandlerId(String handlerId) {
    this.handlerId = handlerId;
  }

  @JacksonXmlProperty(localName = "class", isAttribute = true)
  public Class<Handler> getClazz() {
    return clazz;
  }

  public void setClazz(Class<Handler> clazz) {
    this.clazz = clazz;
  }

  @SuppressWarnings("unchecked")
  public void setClazz(String clazz) throws ClassNotFoundException {
    this.clazz = (Class<Handler>) Class.forName(clazz);
  }
}
