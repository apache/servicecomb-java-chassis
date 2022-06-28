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

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestBodyBufferSupportImpl {
  BodyBufferSupportImpl impl = new BodyBufferSupportImpl();

  @Test
  public void testSetBodyBuffer() {
    impl.setBodyBytes(new byte[] {});
    impl.setBodyLength(10);

    Assertions.assertNotNull(impl.getBodyBytes());
    Assertions.assertEquals(10, impl.getBodyBytesLength());

    impl.setBodyBuffer(null);

    Assertions.assertNull(impl.getBodyBytes());
    Assertions.assertEquals(0, impl.getBodyBytesLength());
  }

  @Test
  public void testGetBodyBuffer() {
    Assertions.assertNull(impl.getBodyBuffer());

    Buffer bodyBuffer = Buffer.buffer();
    impl.setBodyBuffer(bodyBuffer);

    Assertions.assertSame(bodyBuffer, impl.getBodyBuffer());
  }

  @Test
  public void testGetBodyBytes() {
    Assertions.assertNull(impl.getBodyBytes());

    byte[] bytes = new byte[] {1, 2, 3};
    Buffer bodyBuffer = Buffer.buffer(bytes);
    impl.setBodyBuffer(bodyBuffer);

    Assertions.assertArrayEquals(bytes, impl.getBodyBytes());
  }

  @Test
  public void testGetBodyBytesLength() {
    Assertions.assertEquals(0, impl.getBodyBytesLength());

    byte[] bytes = new byte[] {1, 2, 3};
    Buffer bodyBuffer = Buffer.buffer(bytes);
    impl.setBodyBuffer(bodyBuffer);

    Assertions.assertEquals(3, impl.getBodyBytesLength());
  }
}
