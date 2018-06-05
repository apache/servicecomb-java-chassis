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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.servicecomb.foundation.common.part.InputStreamPart;
import org.apache.servicecomb.foundation.vertx.stream.InputStreamToReadStream.ReadResult;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.impl.SyncContext;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class TestPumpFromPart {
  String src = RandomStringUtils.random(100);

  boolean inputStreamClosed;

  InputStream inputStream = new ByteArrayInputStream(src.getBytes()) {
    @Override
    public void close() throws IOException {
      super.close();
      inputStreamClosed = true;
    }
  };

  Part part;

  boolean outputStreamClosed;

  BufferOutputStream outputStream;

  IOException error = new IOException();

  Context context = new SyncContext();

  private void run(Context context, boolean closeOutput) throws Throwable {
    inputStream.reset();
    part = new InputStreamPart("name", inputStream);

    outputStream = new BufferOutputStream() {
      @Override
      public void close() {
        super.close();
        outputStreamClosed = true;
      }
    };

    new PumpFromPart(context, part).toOutputStream(outputStream, closeOutput).get();
  }

  public void do_pump_succ(Context context) throws Throwable {
    run(context, true);

    Assert.assertEquals(src, outputStream.getBuffer().toString());
    Assert.assertTrue(inputStreamClosed);
    Assert.assertTrue(outputStreamClosed);
  }

  @Test
  public void pump_succ() throws Throwable {
    do_pump_succ(null);
    do_pump_succ(context);
  }

  public void do_pump_outputNotClose(Context context) throws Throwable {
    run(context, false);

    Assert.assertEquals(src, outputStream.getBuffer().toString());
    Assert.assertFalse(outputStreamClosed);
  }

  @Test
  public void pump_outputNotClose() throws Throwable {
    do_pump_outputNotClose(null);
    do_pump_outputNotClose(context);
  }

  public void pump_error(Context context) {
    try {
      run(context, true);
      Assert.fail("must throw exception");
    } catch (Throwable e) {
      Assert.assertThat(e, Matchers.instanceOf(ExecutionException.class));
      Assert.assertThat(e.getCause(), Matchers.sameInstance(error));
    }

    Assert.assertTrue(inputStreamClosed);
    Assert.assertTrue(outputStreamClosed);
  }

  @Test
  public void pump_read_error() throws IOException {
    new MockUp<InputStreamToReadStream>() {
      @Mock
      void readInWorker(Future<ReadResult> future) {
        future.fail(error);
      }
    };
    new Expectations(IOUtils.class) {
      {
        IOUtils.copyLarge((InputStream) any, (OutputStream) any);
        result = error;
      }
    };

    pump_error(null);
    pump_error(context);
  }

  @Test
  public void pump_write_error() throws IOException {
    new MockUp<BufferOutputStream>() {
      @Mock
      void write(byte[] b) throws IOException {
        throw error;
      }
    };
    new Expectations(IOUtils.class) {
      {
        IOUtils.copyLarge((InputStream) any, (OutputStream) any);
        result = error;
      }
    };

    pump_error(null);
    pump_error(context);
  }
}
