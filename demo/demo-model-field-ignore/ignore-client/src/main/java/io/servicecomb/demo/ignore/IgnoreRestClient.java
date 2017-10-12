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

package io.servicecomb.demo.ignore;

import org.springframework.web.client.RestTemplate;

import io.servicecomb.demo.TestMgr;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;

public class IgnoreRestClient {
  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    run();

    TestMgr.summary();
  }

  public static void run() throws Exception {
    InputModelForTestIgnore input = new InputModelForTestIgnore("input_id_rest", "input_id_content");
    RestTemplate restTemplate = RestTemplateBuilder.create();

    OutputModelForTestIgnore output = restTemplate
        .postForObject("cse://testModelIgnoreServer/", input, OutputModelForTestIgnore.class);

    TestMgr.check(null, output.getInputId());
    TestMgr.check(input.getContent(), output.getContent());
    TestMgr.check(null, output.getOutputId());
  }
}
