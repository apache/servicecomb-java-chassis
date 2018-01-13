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

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.servicecomb.foundation.common.utils.FortifyUtils;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public final class XmlLoaderUtils {
  private XmlLoaderUtils() {
  }

  private static ObjectMapper xmlMapper = new XmlMapper();

  @SuppressWarnings("unchecked")
  public static <T> T load(Resource res, Class<?> cls) throws Exception {
    return (T) xmlMapper.readValue(res.getURL(), cls);
  }

  public static Document load(URL url) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory dbf = FortifyUtils.getSecurityXmlDocumentFactory();
    // CodeDEX要求xml必须校验
    // 不过这都是用于加载内部配置的，申请例外吧
    dbf.setValidating(false);
    DocumentBuilder db = dbf.newDocumentBuilder();

    return db.parse(url.toString());
  }

  public static Document load(Resource res) throws Exception {
    return load(res.getURL());
  }

  public static Document newDoc() throws ParserConfigurationException {
    DocumentBuilderFactory factory = FortifyUtils.getSecurityXmlDocumentFactory();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.newDocument();
  }

  public static void mergeElement(Element from, Element to) {
    // attrs
    for (int idx = 0; idx < from.getAttributes().getLength(); idx++) {
      Node node = from.getAttributes().item(idx);
      to.getAttributes().setNamedItem(node.cloneNode(false));
    }

    // children
    for (int idx = 0; idx < from.getChildNodes().getLength(); idx++) {
      Node node = from.getChildNodes().item(idx);

      if (!Element.class.isInstance(node)) {
        continue;
      }

      to.appendChild(node.cloneNode(true));
    }
  }
}
