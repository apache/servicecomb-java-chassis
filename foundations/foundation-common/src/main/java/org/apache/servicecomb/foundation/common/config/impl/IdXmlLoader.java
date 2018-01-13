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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class IdXmlLoader extends XmlLoader {
  private Map<String, Element> idMap = new HashMap<>();

  public IdXmlLoader(List<String> locationPatternList) {
    super(locationPatternList);
  }

  public IdXmlLoader(List<String> locationPatternList, String suffix) {
    super(locationPatternList, suffix);
  }

  @Override
  protected Element findAndSetExist(Element ele) {
    String id = ele.getAttribute("id");
    if (StringUtils.isEmpty(id)) {
      throw new RuntimeException("id not allow be empty");
    }

    Element existEle = idMap.get(id);
    if (existEle == null) {
      idMap.put(id, ele);
      return null;
    }

    return existEle;
  }
}
