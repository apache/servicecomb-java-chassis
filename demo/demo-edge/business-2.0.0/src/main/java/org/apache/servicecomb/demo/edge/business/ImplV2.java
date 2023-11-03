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

package org.apache.servicecomb.demo.edge.business;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.demo.edge.model.AppClientDataRsp;
import org.apache.servicecomb.demo.edge.model.ChannelRequestBase;
import org.apache.servicecomb.demo.edge.model.DependTypeA;
import org.apache.servicecomb.demo.edge.model.RecursiveSelfType;
import org.apache.servicecomb.demo.edge.model.ResultWithInstance;
import org.apache.servicecomb.demo.edge.model.User;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestSchema(schemaId = "news-v2")
@RequestMapping(path = "/business/v2")
public class ImplV2 {
  private Environment environment;

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  File tempDir = new File("target/downloadTemp");

  public ImplV2() throws IOException {
    FileUtils.forceMkdir(tempDir);
  }

  @RequestMapping(path = "/channel/news/subscribe", method = RequestMethod.POST)
  public AppClientDataRsp subscribeNewsColumn(@RequestBody ChannelRequestBase request) {
    AppClientDataRsp response = new AppClientDataRsp();
    String rsp = "result from 2.0.0";
    response.setRsp(rsp);
    return response;
  }

  @RequestMapping(path = "/add", method = RequestMethod.GET)
  public ResultWithInstance add(int x, int y) {
    return ResultWithInstance.create(x + y, environment);
  }

  @RequestMapping(path = "/dec", method = RequestMethod.GET)
  public ResultWithInstance dec(int x, int y) {
    return ResultWithInstance.create(x - y, environment);
  }

  @GetMapping(path = "/download")
  @ApiResponses({
      @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = File.class)), description = ""),
  })
  public ResponseEntity<InputStream> download() throws IOException {
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=download.txt")
        .body(new ByteArrayInputStream("download".getBytes(StandardCharsets.UTF_8)));
  }

  protected File createBigFile() throws IOException {
    File file = new File(tempDir, "bigFile.txt");
    file.delete();
    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
    randomAccessFile.setLength(10 * 1024 * 1024);
    randomAccessFile.close();
    return file;
  }

  @GetMapping(path = "/bigFile")
  public File bigFile() throws IOException {
    return createBigFile();
  }

  @PostMapping(path = "recursiveSelf")
  public RecursiveSelfType recursiveSelf(@RequestBody RecursiveSelfType value) {
    return value;
  }

  @PostMapping(path = "dependType")
  public DependTypeA dependType(@RequestBody DependTypeA value) {
    return value;
  }

  @PostMapping(path = "encrypt")
  public User encrypt(@RequestBody User value) {
    return value;
  }
}
