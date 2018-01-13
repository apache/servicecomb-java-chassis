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

import org.apache.servicecomb.foundation.common.config.PaaSResourceUtils;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 无逻辑append xml
 */
public class XmlLoader extends AbstractLoader {

  private String suffix;

  public XmlLoader(List<String> locationPatternList) {
    this(locationPatternList, PaaSResourceUtils.XML_SUFFIX);
  }

  public XmlLoader(List<String> locationPatternList, String suffix) {
    super(locationPatternList);
    this.suffix = suffix;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T load() throws Exception {
    Document doc = XmlLoaderUtils.newDoc();
    Element root = null;
    for (String locationPattern : locationPatternList) {
      List<Resource> resList = PaaSResourceUtils.getSortedResources(locationPattern, suffix);
      for (Resource res : resList) {
        Document tmpDoc = XmlLoaderUtils.load(res);
        Element tmpRoot = tmpDoc.getDocumentElement();

        if (root == null) {
          root = (Element) doc.importNode(tmpRoot, false);
          doc.appendChild(root);
        }

        NodeList nodeList = tmpRoot.getChildNodes();
        for (int idx = 0; idx < nodeList.getLength(); idx++) {
          Node child = nodeList.item(idx);

          if (!Element.class.isInstance(child)) {
            continue;
          }

          Element clone = (Element) doc.importNode(child, true);
          Element exist = findAndSetExist((Element) clone);
          if (exist == null) {
            root.appendChild(clone);
            continue;
          }

          // merge attr and children
          XmlLoaderUtils.mergeElement(clone, exist);
        }
      }
    }
    return (T) doc;
  }

  protected Element findAndSetExist(Element ele) {
    return null;
  }
}
