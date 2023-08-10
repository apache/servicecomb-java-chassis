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
package org.apache.servicecomb.swagger.generator.jaxrs;

import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.jupiter.api.Test;

public class TestClassAnnotation {
  @Test
  public void test_generate_swagger_correct() {
    UnitTestSwaggerUtils.testSwagger("schemas/ClassAnnotation.yaml", ClassAnnotation.class,
        "testBean", "testString", "testForm", "testUpload");
  }

  @Test
  public void test_form_wrong() {
    UnitTestSwaggerUtils.testException("generate swagger operation failed, "
            + "method=org.apache.servicecomb.swagger.generator.jaxrs.ClassAnnotation:testFormWrong.",
        ClassAnnotation.class,
        "testFormWrong");
  }

  @Test
  public void test_upload_wrong() {
    UnitTestSwaggerUtils.testException("generate swagger operation failed, "
            + "method=org.apache.servicecomb.swagger.generator.jaxrs.ClassAnnotation:testUploadWrong.",
        ClassAnnotation.class,
        "testUploadWrong");
  }
}
