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

package org.apache.servicecomb.foundation.vertx.http;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.buffer.Buffer;
import mockit.Deencapsulation;

public class TestBodyBufferSupportImpl {
  BodyBufferSupportImpl impl = new BodyBufferSupportImpl();

  @Test
  public void testSetBodyBuffer() {
    Deencapsulation.setField(impl, "bodyBytes", new byte[] {});
    Deencapsulation.setField(impl, "bodyLength", 10);

    Assert.assertNotNull(impl.getBodyBytes());
    Assert.assertEquals(10, impl.getBodyBytesLength());

    impl.setBodyBuffer(null);

    Assert.assertNull(impl.getBodyBytes());
    Assert.assertEquals(0, impl.getBodyBytesLength());
  }

  @Test
  public void testGetBodyBuffer() {
    Assert.assertNull(impl.getBodyBuffer());

    Buffer bodyBuffer = Buffer.buffer();
    impl.setBodyBuffer(bodyBuffer);

    Assert.assertSame(bodyBuffer, impl.getBodyBuffer());
  }

  @Test
  public void testGetBodyBytes() {
    Assert.assertNull(impl.getBodyBytes());

    byte[] bytes = new byte[] {1, 2, 3};
    Buffer bodyBuffer = Buffer.buffer(bytes);
    impl.setBodyBuffer(bodyBuffer);

    Assert.assertArrayEquals(bytes, impl.getBodyBytes());
  }

  @Test
  public void testGetBodyBytesLength() {
    Assert.assertEquals(0, impl.getBodyBytesLength());

    byte[] bytes = new byte[] {1, 2, 3};
    Buffer bodyBuffer = Buffer.buffer(bytes);
    impl.setBodyBuffer(bodyBuffer);

    Assert.assertEquals(3, impl.getBodyBytesLength());
  }
}
