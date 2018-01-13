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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

public class TestReferenceDefParser {

  @Test
  public void testDoParse()
      throws Exception {

    ReferenceDefParser lReferenceDefParser = new ReferenceDefParser();

    Element element = Mockito.mock(Element.class);
    Mockito.when(element.getAttribute(PojoConst.SCHEMA_ID)).thenReturn("abc");
    lReferenceDefParser.doParse(element, null, Mockito.mock(BeanDefinitionBuilder.class));
    Assert.assertEquals(PojoReferenceMeta.class, lReferenceDefParser.getBeanClass(null));
  }
}
