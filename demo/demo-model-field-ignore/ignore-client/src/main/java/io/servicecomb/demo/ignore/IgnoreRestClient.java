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
