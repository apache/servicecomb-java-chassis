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
package org.apache.servicecomb.it.testcase;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.foundation.vertx.http.ReadStreamPart;
import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.testcase.support.DownloadSchemaIntf;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.Iterables;

public class TestDownload {
  private File dir = new File("target/download");

  private static Consumers<DownloadSchemaIntf> consumers = new Consumers<>("download",
      DownloadSchemaIntf.class);

  private List<CompletableFuture<?>> futures = new ArrayList<>();

  private String content = "file content";

  public TestDownload() {
    FileUtils.deleteQuietly(dir);
  }

  private String readFileToString(File file) {
    try {
      return FileUtils.readFileToString(file);
    } catch (IOException e) {
      return "read file failed:" + e.getMessage();
    }
  }

  private CompletableFuture<File> checkFile(ReadStreamPart part) {
    CompletableFuture<File> future = part.saveToFile("target/download/"
        + UUID.randomUUID().toString()
        + "-"
        + part.getSubmittedFileName());
    return checkFuture(future);
  }

  private String getStackTrace(Throwable e) {
    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  private <T> CompletableFuture<T> checkFuture(CompletableFuture<T> future) {
    Error error = new Error();
    future.whenComplete((result, e) -> {
      Object value = result;
      if (File.class.isInstance(value)) {
        File file = (File) value;
        value = readFileToString(file);
        file.delete();
      } else if (byte[].class.isInstance(value)) {
        value = new String((byte[]) value);
      }

      Assert.assertEquals(getStackTrace(error), content, value);
    });

    return future;
  }

  private ReadStreamPart templateGet(String methodPath) {
    return consumers.getSCBRestTemplate()
        .getForObject("/" + methodPath + "?content={content}",
            ReadStreamPart.class,
            content);
  }

  private ReadStreamPart templateExchange(String methodPath, String type) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("accept", type);
    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<ReadStreamPart> response = consumers.getSCBRestTemplate()
        .exchange("/" + methodPath + "?content={content}",
            HttpMethod.GET,
            entity,
            ReadStreamPart.class, content);
    return response.getBody();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void runRest() {
    futures.add(checkFile(consumers.getIntf().tempFileEntity(content)));
    futures.add(checkFuture(templateGet("tempFileEntity").saveAsBytes()));
    futures.add(checkFuture(templateExchange("tempFileEntity", MediaType.TEXT_PLAIN_VALUE).saveAsBytes()));
    futures.add(checkFuture(templateExchange("tempFileEntity", MediaType.APPLICATION_JSON_VALUE).saveAsBytes()));

    futures.add(checkFile(consumers.getIntf().tempFilePart(content)));
    futures.add(checkFuture(templateGet("tempFilePart").saveAsString()));
    futures.add(checkFuture(templateExchange("tempFilePart", MediaType.TEXT_PLAIN_VALUE).saveAsString()));
    futures.add(checkFuture(templateExchange("tempFilePart", MediaType.APPLICATION_JSON_VALUE).saveAsString()));

    futures.add(checkFile(consumers.getIntf().file(content)));
    futures.add(checkFuture(templateGet("file").saveAsString()));
    futures.add(checkFuture(templateExchange("file", MediaType.TEXT_PLAIN_VALUE).saveAsString()));
    futures.add(checkFuture(templateExchange("file", MediaType.APPLICATION_JSON_VALUE).saveAsString()));

    {
      ReadStreamPart part = consumers.getIntf().chineseAndSpaceFile(content);
      Assert.assertEquals("测 试.test.txt", part.getSubmittedFileName());
      futures.add(checkFile(part));

      part = templateGet("chineseAndSpaceFile");
      Assert.assertEquals("测 试.test.txt", part.getSubmittedFileName());
      futures.add(checkFuture(part.saveAsString()));

      ReadStreamPart part2 = templateExchange("chineseAndSpaceFile", MediaType.TEXT_PLAIN_VALUE);
      Assert.assertEquals("测 试.test.txt", part2.getSubmittedFileName());
      futures.add(checkFuture(part2.saveAsString()));

      ReadStreamPart part3 = templateExchange("chineseAndSpaceFile", MediaType.APPLICATION_JSON_VALUE);
      Assert.assertEquals("测 试.test.txt", part3.getSubmittedFileName());
      futures.add(checkFuture(part3.saveAsString()));
    }

    futures.add(checkFile(consumers.getIntf().resource(content)));
    futures.add(checkFuture(templateGet("resource").saveAsString()));
    futures.add(checkFuture(templateExchange("resource", MediaType.TEXT_PLAIN_VALUE).saveAsString()));
    futures.add(checkFuture(templateExchange("resource", MediaType.APPLICATION_JSON_VALUE).saveAsString()));

    futures.add(checkFile(consumers.getIntf().entityResource(content)));
    futures.add(checkFuture(templateGet("entityResource").saveAsString()));
    futures.add(checkFuture(templateExchange("entityResource", MediaType.TEXT_PLAIN_VALUE).saveAsString()));
    futures.add(checkFuture(templateExchange("entityResource", MediaType.APPLICATION_JSON_VALUE).saveAsString()));

    futures.add(checkFile(consumers.getIntf().entityInputStream(content)));
    futures.add(checkFuture(templateGet("entityInputStream").saveAsString()));
    futures.add(checkFuture(templateExchange("entityInputStream", MediaType.TEXT_PLAIN_VALUE).saveAsString()));
    futures.add(checkFuture(templateExchange("entityInputStream", MediaType.APPLICATION_JSON_VALUE).saveAsString()));

    futures.add(checkFile(consumers.getIntf().bytes(content)));
    futures.add(checkFuture(templateGet("bytes").saveAsString()));
    futures.add(checkFuture(templateExchange("bytes", MediaType.TEXT_PLAIN_VALUE).saveAsString()));
    futures.add(checkFuture(templateExchange("bytes", MediaType.APPLICATION_JSON_VALUE).saveAsString()));

    futures.add(checkFile(consumers.getIntf().netInputStream(content)));
    futures.add(checkFuture(templateGet("netInputStream").saveAsString()));
    futures.add(checkFuture(templateExchange("netInputStream", MediaType.TEXT_PLAIN_VALUE).saveAsString()));
    futures.add(checkFuture(templateExchange("netInputStream", MediaType.APPLICATION_JSON_VALUE).saveAsString()));

    try {
      CompletableFuture
          .allOf(Iterables.toArray((List<CompletableFuture<Object>>) (Object) futures, CompletableFuture.class))
          .get();
    } catch (InterruptedException | ExecutionException e1) {
      Assert.fail("test download failed: " + getStackTrace(e1));
    }
  }
}
