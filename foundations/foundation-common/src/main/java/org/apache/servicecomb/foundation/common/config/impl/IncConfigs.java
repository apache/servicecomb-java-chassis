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

package org.apache.servicecomb.foundation.common.config.impl;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "configs")
public class IncConfigs {

  public static class IncConfig {
    @JacksonXmlProperty(isAttribute = true)
    private String id;

    @JacksonXmlProperty(isAttribute = true)
    private String loader;

    @JacksonXmlProperty(localName = "path")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<String> pathList;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getLoader() {
      return loader;
    }

    public void setLoader(String loader) {
      this.loader = loader;
    }

    public List<String> getPathList() {
      return pathList;
    }

    public void setPathList(List<String> pathList) {
      this.pathList = pathList;
    }
  }

  @JacksonXmlProperty(localName = "properties")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<IncConfig> propertiesList;

  @JacksonXmlProperty(localName = "xml")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<IncConfig> xmlList;

  public List<IncConfig> getPropertiesList() {
    return propertiesList;
  }

  public void setPropertiesList(List<IncConfig> propertiesList) {
    this.propertiesList = propertiesList;
  }

  public List<IncConfig> getXmlList() {
    return xmlList;
  }

  public void setXmlList(List<IncConfig> xmlList) {
    this.xmlList = xmlList;
  }
}
