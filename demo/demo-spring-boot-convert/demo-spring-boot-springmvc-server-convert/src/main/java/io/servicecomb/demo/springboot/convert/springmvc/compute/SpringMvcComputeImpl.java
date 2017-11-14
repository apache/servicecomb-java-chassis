/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.demo.springboot.convert.springmvc.compute;

import io.servicecomb.provider.rest.common.RestSchema;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RestSchema(schemaId = "springMvcCompute")
@Controller
@RequestMapping(path = "/springmvccompute", produces = MediaType.APPLICATION_JSON)
public class SpringMvcComputeImpl implements SpringMvcCompute {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcComputeImpl.class);

  @Override
  @RequestMapping(path = "/add", method = RequestMethod.GET)
  @ResponseBody
  public int add(int a, int b) {
    LOGGER.info("SpringMvcComputeImpl.add() is called, a = {}, b = {}", a, b);
    return a + b;
  }
}
