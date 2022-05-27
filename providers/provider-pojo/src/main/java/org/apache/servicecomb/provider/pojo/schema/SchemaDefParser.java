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

package org.apache.servicecomb.provider.pojo.schema;

import org.apache.servicecomb.provider.pojo.PojoConst;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class SchemaDefParser extends AbstractSingleBeanDefinitionParser {
  @Override
  protected boolean shouldGenerateId() {
    return true;
  }

  @Override
  protected boolean shouldParseNameAsAliases() {
    return false;
  }

  @Override
  protected Class<?> getBeanClass(Element element) {
    return PojoProducerMeta.class;
  }

  @Override
  protected void doParse(Element element, ParserContext parserContext,
      BeanDefinitionBuilder builder) {
    builder.addPropertyValue(PojoConst.FIELD_SCHEMA_ID, element.getAttribute(PojoConst.SCHEMA_ID));
    builder.addPropertyValue(PojoConst.IMPL, element.getAttribute(PojoConst.IMPL));
    builder.addPropertyValue(PojoConst.FIELD_SCHEMA_INTERFACE, element.getAttribute(PojoConst.SCHEMA_INTERFACE));
  }
}
