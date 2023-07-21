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

package org.apache.servicecomb.swagger.invocation.converter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import jakarta.servlet.http.Part;

import org.apache.servicecomb.foundation.common.Holder;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestPartToMultipartFile {
  @Mocked
  Part part;

  PartToMultipartFile multipartFile;

  @Before
  public void setup() {
    multipartFile = new PartToMultipartFile(part);
  }

  @Test
  public void getName() {
    String name = "paramName";
    new Expectations() {
      {
        part.getName();
        result = name;
      }
    };

    Assertions.assertEquals(name, multipartFile.getName());
  }

  @Test
  public void getOriginalFilename() {
    String submittedFileName = "fileName";
    new Expectations() {
      {
        part.getSubmittedFileName();
        result = submittedFileName;
      }
    };

    Assertions.assertEquals(submittedFileName, multipartFile.getOriginalFilename());
  }

  @Test
  public void getContentType() {
    String contentType = "json";
    new Expectations() {
      {
        part.getContentType();
        result = contentType;
      }
    };

    Assertions.assertEquals(contentType, multipartFile.getContentType());
  }

  @Test
  public void isEmptyTrue() {
    new Expectations() {
      {
        part.getSize();
        result = 0;
      }
    };

    Assertions.assertTrue(multipartFile.isEmpty());
  }

  @Test
  public void isEmptyFalse() {
    new Expectations() {
      {
        part.getSize();
        result = 1;
      }
    };

    Assertions.assertFalse(multipartFile.isEmpty());
  }

  @Test
  public void getSize() {
    long size = 10;
    new Expectations() {
      {
        part.getSize();
        result = size;
      }
    };

    Assertions.assertEquals(size, multipartFile.getSize());
  }

  static class ByteArrayInputStreamForTest extends ByteArrayInputStream {
    boolean closed;

    public ByteArrayInputStreamForTest(byte[] buf) {
      super(buf);
    }

    @Override
    public void close() throws IOException {
      closed = true;
    }
  }

  @Test
  public void getBytes_normal() throws IOException {
    byte[] bytes = new byte[] {1, 2, 3};
    ByteArrayInputStreamForTest is = new ByteArrayInputStreamForTest(bytes);
    new Expectations() {
      {
        part.getInputStream();
        result = is;
      }
    };

    Assertions.assertArrayEquals(bytes, multipartFile.getBytes());
    Assertions.assertTrue(is.closed);
  }

  @Test
  public void getBytes_exception() throws IOException {
    new Expectations() {
      {
        part.getInputStream();
        result = new IOException("open stream failed");
      }
    };

    IOException exception = Assertions.assertThrows(IOException.class,
            () -> multipartFile.getBytes());
    Assertions.assertEquals("open stream failed", exception.getMessage());
  }

  @Test
  public void transferTo() throws IllegalStateException, IOException {
    File dest = new File("/dest");
    Holder<String> destName = new Holder<>();
    new MockUp<Part>(part) {
      @Mock
      void write(String fileName) throws IOException {
        destName.value = fileName;
      }
    };

    multipartFile.transferTo(dest);
    Assertions.assertEquals(dest.getPath(), destName.value);
  }
}
