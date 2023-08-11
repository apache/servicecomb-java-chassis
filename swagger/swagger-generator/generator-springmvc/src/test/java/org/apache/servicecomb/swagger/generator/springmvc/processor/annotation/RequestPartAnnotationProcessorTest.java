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

package org.apache.servicecomb.swagger.generator.springmvc.processor.annotation;

import java.util.List;

import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public class RequestPartAnnotationProcessorTest {
  @Test
  public void testDemoRest() {
    UnitTestSwaggerUtils.testSwagger("schemas/DemoRest.yaml", DemoRest.class);
  }

  @RequestMapping("/")
  public static class DemoRest {
    @RequestMapping(method = RequestMethod.POST, path = "/fun")
    public void fun(@RequestPart("stringParam") String stringParam,
        @RequestPart(name = "intParam") int intParam,
        @RequestPart("stringParamArray") String[] stringParamArray,
        @RequestPart("stringParamCollection") List<String> stringParamCollection,
        @RequestPart("file") MultipartFile file,
        @RequestPart("fileArray") MultipartFile[] fileArray,
        @RequestPart("fileCollection") List<MultipartFile> fileCollection) {
    }
  }
}
