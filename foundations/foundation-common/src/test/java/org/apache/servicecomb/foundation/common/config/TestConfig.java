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

package org.apache.servicecomb.foundation.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.servicecomb.foundation.common.config.impl.IdXmlLoader;
import org.apache.servicecomb.foundation.common.config.impl.PaaSPropertiesLoaderUtils;
import org.apache.servicecomb.foundation.common.config.impl.XmlLoader;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestConfig {
  private static final int TEST_PROP_LIST_SIZE = 2;

  private static ApplicationContext context;

  @Test
  public void loadMergedProperties() throws Exception {
    Properties prop = ConfigMgr.INSTANCE.getConfig("pTest");
    Assert.assertEquals("0", prop.get("1"));
    Assert.assertEquals("1", prop.get("1.1"));
    Assert.assertEquals("2", prop.get("1.2"));
  }

  @Test
  public void testBean() throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
    BeanProp bp = (BeanProp) BeanUtils.getBean("beanProp");
    Assert.assertEquals("2", bp.getTest());
  }

  @Test
  public void testBeanContext() {
    BeanUtils.setContext(context);
    Assert.assertEquals(context, BeanUtils.getContext());
  }

  @Test
  public void testConvertProperties() throws Exception {
    BeanProp bp = PaaSResourceUtils.loadConfigAs("pTest", BeanProp.class);
    Assert.assertEquals("2", bp.getTest());
  }

  @Test
  public void testConvertXml() throws Exception {
    Object ret = PaaSResourceUtils.loadConfigAs("xTest", BeanProp.class);
    BeanProp bp = (BeanProp) ret;
    Assert.assertEquals("test value", bp.getTest());
  }

  @Test
  public void testXml() throws Exception {
    List<String> locationPatternList = new ArrayList<>();
    locationPatternList.add("classpath*:config/config.test.inc.xml");
    ConfigLoader loader = new XmlLoader(locationPatternList, ".inc.xml");
    Document doc = loader.load();

    Element root = doc.getDocumentElement();
    Assert.assertEquals("configs", root.getNodeName());

    {
      NodeList propList = root.getElementsByTagName("properties");
      Assert.assertEquals(2, propList.getLength());

      {
        Element prop = (Element) propList.item(0);
        Assert.assertEquals("pTest", prop.getAttributes().getNamedItem("id").getNodeValue());

        NodeList pathList = prop.getElementsByTagName("path");
        Assert.assertEquals(1, pathList.getLength());
        Assert.assertEquals("classpath*:config/test.properties", pathList.item(0).getTextContent());
      }

      {
        Element prop = (Element) propList.item(1);
        Assert.assertEquals("pTest", prop.getAttributes().getNamedItem("id").getNodeValue());

        NodeList pathList = prop.getElementsByTagName("path");
        Assert.assertEquals(1, pathList.getLength());
        Assert.assertEquals("classpath*:config/test.ext.properties",
            pathList.item(0).getTextContent());
      }
    }

    {
      NodeList xmlList = root.getElementsByTagName("xml");
      Assert.assertEquals(2, xmlList.getLength());

      {
        Element xml = (Element) xmlList.item(0);
        Assert.assertEquals("xTest", xml.getAttributes().getNamedItem("id").getNodeValue());

        NodeList pathList = xml.getElementsByTagName("path");
        Assert.assertEquals(1, pathList.getLength());
        Assert.assertEquals("classpath*:config/test.xml", pathList.item(0).getTextContent());
      }

      {
        Element xml = (Element) xmlList.item(1);
        Assert.assertEquals("xTest", xml.getAttributes().getNamedItem("id").getNodeValue());

        NodeList pathList = xml.getElementsByTagName("path");
        Assert.assertEquals(1, pathList.getLength());
        Assert.assertEquals("classpath*:config/test.ext.xml", pathList.item(0).getTextContent());
      }
    }
  }

  @Test
  public void testIdXml() throws Exception {
    List<String> locationPatternList = new ArrayList<>();
    locationPatternList.add("classpath*:config/config.test.inc.xml");
    ConfigLoader loader = new IdXmlLoader(locationPatternList, ".inc.xml");
    Document doc = loader.load();

    Element root = doc.getDocumentElement();
    Assert.assertEquals("configs", root.getNodeName());

    NodeList propList = root.getElementsByTagName("properties");
    Assert.assertEquals(1, propList.getLength());

    Element prop = (Element) propList.item(0);
    Assert.assertEquals("pTest", prop.getAttributes().getNamedItem("id").getNodeValue());

    NodeList pathListProp = prop.getElementsByTagName("path");
    Assert.assertEquals(TEST_PROP_LIST_SIZE, pathListProp.getLength());
    Assert.assertEquals("classpath*:config/test.properties", pathListProp.item(0).getTextContent());
    Assert.assertEquals("classpath*:config/test.ext.properties", pathListProp.item(1).getTextContent());

    NodeList xmlList = root.getElementsByTagName("xml");
    Assert.assertEquals(1, xmlList.getLength());

    Element xml = (Element) xmlList.item(0);
    Assert.assertEquals("xTest", xml.getAttributes().getNamedItem("id").getNodeValue());

    NodeList pathListXml = xml.getElementsByTagName("path");
    Assert.assertEquals(TEST_PROP_LIST_SIZE, pathListXml.getLength());
    Assert.assertEquals("classpath*:config/test.xml", pathListXml.item(0).getTextContent());
    Assert.assertEquals("classpath*:config/test.ext.xml", pathListXml.item(1).getTextContent());
  }

  @Test
  public void testPaaSResourceUtils() throws Exception {
    List<Resource> oList = PaaSResourceUtils.getSortedXmls("test.xml");
    Assert.assertNotEquals(null, oList);
    Assert.assertNotEquals(null, PaaSResourceUtils.loadMergedProperties("config/test.properties"));
    PaaSResourceUtils.sortProperties(oList);
    PaaSResourceUtils.sortXmls(oList);
    Assert.assertNotEquals(null, PaaSPropertiesLoaderUtils.loadMergedProperties("config/test.properties"));
    try {
      PaaSPropertiesLoaderUtils.fillMergedProperties(new Properties(), "");
    } catch (Exception e) {
      Assert.assertEquals(true, (e.getMessage()).contains("Resource path must not be null or empty"));
    }
    try {
      PaaSPropertiesLoaderUtils.fillMergedProperties(new Properties(), "tes.kunle");
    } catch (Exception e) {
      Assert.assertEquals(true, (e.getMessage()).contains("Resource path must ends with"));
    }
  }
}
