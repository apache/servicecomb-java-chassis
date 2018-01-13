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

package org.apache.servicecomb.provider.pojo.reference;

import org.apache.servicecomb.provider.pojo.PojoConst;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class ReferenceDefParser extends AbstractSingleBeanDefinitionParser {
  @Override
  protected Class<?> getBeanClass(Element element) {
    return PojoReferenceMeta.class;
  }

  @Override
  protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    builder.addPropertyValue(PojoConst.FIELD_MICROSERVICE_NAME,
        element.getAttribute(PojoConst.MICROSERVICE_NAME));

    String schemaId = element.getAttribute(PojoConst.SCHEMA_ID);
    String intf = element.getAttribute(PojoConst.INTERFACE);

    if (StringUtils.isEmpty(intf) && !StringUtils.isEmpty(schemaId)) {
      //  尝试将schemaId当作接口名使用
      Class<?> consumerIntf = ClassUtils.getClassByName(null, schemaId);
      if (consumerIntf != null) {
        intf = schemaId;
      }
    }

    builder.addPropertyValue(PojoConst.FIELD_SCHEMA_ID, schemaId);
    builder.addPropertyValue(PojoConst.FIELD_INTERFACE, intf);

    if (StringUtils.isEmpty(schemaId) && StringUtils.isEmpty(intf)) {
      throw new Error("schema-id and interface can not both be empty.");
    }
  }
}
