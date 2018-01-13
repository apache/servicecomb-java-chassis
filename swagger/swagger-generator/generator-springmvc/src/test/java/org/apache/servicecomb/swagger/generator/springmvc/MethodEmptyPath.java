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

package org.apache.servicecomb.swagger.generator.springmvc;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiOperation;

@RequestMapping(path = "MethodEmptyPath")
public class MethodEmptyPath {

  @GetMapping
  public void usingGetMapping() {

  }

  @PostMapping
  public void usingPostMapping() {

  }

  @PutMapping
  public void usingPutMapping() {

  }

  @DeleteMapping
  public void usingDeleteMapping() {

  }

  @PatchMapping
  public void usingPatchMapping() {

  }

  // this will be ignored in the generation of service contract
  // as ApiOperation is not a restful annotation
  @ApiOperation(value = "")
  public void ignoredNonRestful() {

  }
}
