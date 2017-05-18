/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.foundation.common.config.impl;

import java.util.List;

import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.huawei.paas.foundation.common.config.PaaSResourceUtils;

/**
 * 无逻辑append xml
 * @author   
 * @version  [版本号, 2016年11月21日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class XmlLoader extends AbstractLoader {

    private String suffix;

    /**
     * <构造函数>
     * @param locationPatternList locationPatternList
     */
    public XmlLoader(List<String> locationPatternList) {
        this(locationPatternList, PaaSResourceUtils.XML_SUFFIX);
    }

    /**
     * <构造函数>
     * @param locationPatternList locationPatternList
     * @param suffix suffix
     */
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

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param ele ele
     * @return Element
     */
    protected Element findAndSetExist(Element ele) {
        return null;
    }
}
