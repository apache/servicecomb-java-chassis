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

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper extends InputStream {
  private final InputStream inputStream;

  public InputStreamWrapper(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  @Override
  public int read() throws IOException {
    return inputStream.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return inputStream.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return inputStream.read(b, off, len);
  }

  @Override
  public long skip(long n) throws IOException {
    return inputStream.skip(n);
  }

  @Override
  public int available() throws IOException {
    return inputStream.available();
  }

  @Override
  public boolean markSupported() {
    return inputStream.markSupported();
  }

  @Override
  public synchronized void mark(int readlimit) {
    inputStream.mark(readlimit);
  }

  @Override
  public void close() throws IOException {
    FileUploadStreamRecorder.getInstance().clearRecorder(this);
    inputStream.close();
  }

  @Override
  public synchronized void reset() throws IOException {
    inputStream.reset();
  }
}
