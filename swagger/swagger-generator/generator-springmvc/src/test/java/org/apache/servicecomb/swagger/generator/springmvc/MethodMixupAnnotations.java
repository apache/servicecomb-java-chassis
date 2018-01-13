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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "MethodMixupAnnotations")
public class MethodMixupAnnotations {
  @RequestMapping(
      path = "usingRequestMapping/{targetName}",
      method = {RequestMethod.POST},
      consumes = {"text/plain", "application/*"},
      produces = {"text/plain", "application/*"})
  public String usingRequestMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word, @RequestAttribute String form) {
    return String.format("%s %s %s %s %s", srcUser.name, header, targetName, word, form);
  }

  @GetMapping(
      path = "usingGetMapping/{targetName}",
      consumes = {"text/plain", "application/*"},
      produces = {"text/plain", "application/*"})
  public String usingGetMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word, @RequestAttribute String form) {
    return String.format("%s %s %s %s %s", srcUser.name, header, targetName, word, form);
  }

  @PutMapping(
      path = "usingPutMapping/{targetName}",
      consumes = {"text/plain", "application/*"},
      produces = {"text/plain", "application/*"})
  public String usingPutMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word, @RequestAttribute String form) {
    return String.format("%s %s %s %s %s", srcUser.name, header, targetName, word, form);
  }

  @PostMapping(
      path = "usingPostMapping/{targetName}",
      consumes = {"text/plain", "application/*"},
      produces = {"text/plain", "application/*"})
  public String usingPostMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word, @RequestAttribute String form) {
    return String.format("%s %s %s %s %s", srcUser.name, header, targetName, word, form);
  }

  @PatchMapping(
      path = "usingPatchMapping/{targetName}",
      consumes = {"text/plain", "application/*"},
      produces = {"text/plain", "application/*"})
  public String usingPatchMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word, @RequestAttribute String form) {
    return String.format("%s %s %s %s %s", srcUser.name, header, targetName, word, form);
  }

  @DeleteMapping(
      path = "usingDeleteMapping/{targetName}",
      consumes = {"text/plain", "application/*"},
      produces = {"text/plain", "application/*"})
  public String usingDeleteMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word, @RequestAttribute String form) {
    return String.format("%s %s %s %s %s", srcUser.name, header, targetName, word, form);
  }
}
