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

package io.servicecomb.foundation.common.config.impl;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @version  [版本号, 2016年11月21日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@JacksonXmlRootElement(localName = "configs")
public class IncConfigs {
    /**
     * <一句话功能简述>
     * <功能详细描述>
     *
     */
    public static class IncConfig {
        /**
         * id
         */
        @JacksonXmlProperty(isAttribute = true)
        private String id;

        /**
         * loader
         */
        @JacksonXmlProperty(isAttribute = true)
        private String loader;

        /**
         * pathList
         */
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

    /**
     * propertiesList
     */
    @JacksonXmlProperty(localName = "properties")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<IncConfig> propertiesList;

    /**
     * xmlList
     */
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
