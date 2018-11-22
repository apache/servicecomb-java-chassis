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
package org.apache.servicecomb.foundation.vertx.stream;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import mockit.Expectations;
import mockit.Mocked;

public class TestPumpImpl {

  @Test
  public void testPumpWithPending(@Mocked ReadStream<Object> rs, @Mocked WriteStream<Object> ws, @Mocked Buffer zeroBuf,
      @Mocked Buffer contentBuf) {
    PumpImpl<Object> pump = new PumpImpl<>(rs, ws);
    Handler<Object> handler = pump.getDataHandler();
    new Expectations() {
      {
        zeroBuf.length();
        result = 0;
        contentBuf.length();
        result = 1;
      }
    };
    handler.handle(zeroBuf);
    handler.handle(contentBuf);
    Assert.assertEquals(1, pump.numberPumped());
    handler.handle(contentBuf);
    Assert.assertEquals(2, pump.numberPumped());
  }
}
