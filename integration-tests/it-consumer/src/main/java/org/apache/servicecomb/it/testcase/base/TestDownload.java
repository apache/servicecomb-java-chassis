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
package org.apache.servicecomb.it.testcase.base;

import org.junit.Assert;
import org.junit.Test;

public class TestDownload {
  //  private static DownloadSchemaIntf intf = Invoker
  //      .createProxy("it-producer", "download", DownloadSchemaIntf.class);

  static int x;

  @Test
  public void test1() {
    x++;
    System.out.println("test" + x);
//    intf.tempFileEntity("abc");
  }

  @Test
  public void test2() {
    x++;
    System.out.println("test2");
    Assert.assertEquals(x, x + 1);
  }
//  private File dir = new File("target/download");
//
//
//  private RestTemplate restTemplate = new CseRestTemplate();
//
//  private String prefix = "cse://springmvc/download";
//
//  private List<CompletableFuture<?>> futures = new ArrayList<>();
//
//  private String content = "file content";
//
//  public TestDownload() {
//    FileUtils.deleteQuietly(dir);
//  }
//
//  private String readFileToString(File file) {
//    try {
//      return FileUtils.readFileToString(file);
//    } catch (IOException e) {
//      return "read file failed:" + e.getMessage();
//    }
//  }
//
//  private CompletableFuture<File> checkFile(ReadStreamPart part) {
//    CompletableFuture<File> future = part.saveToFile("target/download/"
//        + UUID.randomUUID().toString()
//        + "-"
//        + part.getSubmittedFileName());
//    return checkFuture(future);
//  }
//
//  private <T> CompletableFuture<T> checkFuture(CompletableFuture<T> future) {
//    Error error = new Error();
//    future.whenComplete((result, e) -> {
//      Object value = result;
//      if (File.class.isInstance(value)) {
//        File file = (File) value;
//        value = readFileToString(file);
//        file.delete();
//      } else if (byte[].class.isInstance(value)) {
//        value = new String((byte[]) value);
//      }
//
//      TestMgr.check(content, value, error);
//    });
//
//    return future;
//  }
//
//  private ReadStreamPart templateGet(String methodPath) {
//    return restTemplate
//        .getForObject(prefix + "/" + methodPath + "?content={content}",
//            ReadStreamPart.class,
//            content);
//  }
//
//  public void runRest() {
//    futures.add(checkFile(intf.tempFileEntity(content)));
//    futures.add(checkFuture(templateGet("tempFileEntity").saveAsBytes()));
//
//    futures.add(checkFile(intf.tempFilePart(content)));
//    futures.add(checkFuture(templateGet("tempFilePart").saveAsString()));
//
//    futures.add(checkFile(intf.file(content)));
//    futures.add(checkFuture(templateGet("file").saveAsString()));
//
//    {
//      ReadStreamPart part = intf.chineseAndSpaceFile(content);
//      TestMgr.check("测 试.test.txt", part.getSubmittedFileName());
//      futures.add(checkFile(part));
//
//      part = templateGet("chineseAndSpaceFile");
//      TestMgr.check("测 试.test.txt", part.getSubmittedFileName());
//      futures.add(checkFuture(part.saveAsString()));
//    }
//
//    futures.add(checkFile(intf.resource(content)));
//    futures.add(checkFuture(templateGet("resource").saveAsString()));
//
//    futures.add(checkFile(intf.entityResource(content)));
//    futures.add(checkFuture(templateGet("entityResource").saveAsString()));
//
//    futures.add(checkFile(intf.entityInputStream(content)));
//    futures.add(checkFuture(templateGet("entityInputStream").saveAsString()));
//
//    futures.add(checkFile(intf.bytes(content)));
//    futures.add(checkFuture(templateGet("bytes").saveAsString()));
//
//    futures.add(checkFile(intf.netInputStream(content)));
//    futures.add(checkFuture(templateGet("netInputStream").saveAsString()));
//
//    try {
//      CompletableFuture
//          .allOf(futures.toArray(new CompletableFuture[futures.size()]))
//          .get();
//    } catch (InterruptedException | ExecutionException e1) {
//      TestMgr.failed("test download failed.", e1);
//    }
//  }
}
