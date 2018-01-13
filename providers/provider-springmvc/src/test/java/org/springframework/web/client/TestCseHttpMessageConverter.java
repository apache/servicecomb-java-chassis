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

package org.springframework.web.client;

import java.io.IOException;

import org.apache.servicecomb.provider.common.MockUtil;
import org.apache.servicecomb.provider.springmvc.reference.CseClientHttpRequest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class TestCseHttpMessageConverter {

  @Test
  public void testAll() {
    MockUtil.getInstance().mockReflectionUtils();
    MockUtil.getInstance().mockCseClientHttpRequest();
    CseHttpMessageConverter lCseHttpMessageConverter = new CseHttpMessageConverter();
    lCseHttpMessageConverter.canWrite(null, null);
    lCseHttpMessageConverter.getSupportedMediaTypes();
    try {
      lCseHttpMessageConverter.read(this.getClass(), null);
    } catch (HttpMessageNotReadableException | IOException ignored) {
    }
    try {
      HttpOutputMessage httpOutputMessage = Mockito.mock(CseClientHttpRequest.class);
      lCseHttpMessageConverter.write(null, null, httpOutputMessage);
    } catch (HttpMessageNotWritableException | IOException ignored) {
    }

    Assert.assertEquals(true, lCseHttpMessageConverter.canRead(null, null));
  }
}
