/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx.http;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestAbstractHttpServletRequest {
  @Test
  public void testAttribute() {
    HttpServletRequest request = new AbstractHttpServletRequest() {
    };

    String key = "a1";
    String value = "abc";
    request.setAttribute(key, value);
    Assert.assertSame(value, request.getAttribute(key));
    Assert.assertThat(Collections.list(request.getAttributeNames()), Matchers.contains(key));

    request.setAttribute("a2", "v");
    Assert.assertThat(Collections.list(request.getAttributeNames()), Matchers.contains(key, "a2"));

    request.removeAttribute(key);
    Assert.assertNull(request.getAttribute(key));
  }
}
