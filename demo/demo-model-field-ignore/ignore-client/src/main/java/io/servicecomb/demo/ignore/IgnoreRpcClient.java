package io.servicecomb.demo.ignore;

import org.springframework.stereotype.Component;

import io.servicecomb.demo.TestMgr;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;
import io.servicecomb.provider.pojo.RpcReference;

@Component
public class IgnoreRpcClient {
  @RpcReference(microserviceName = "testModelIgnoreServer", schemaId = "ignoreServiceProvider")
  private static Service service;

  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    run();

    TestMgr.summary();
  }

  public static void run() throws Exception {
    InputModelForTestIgnore input = new InputModelForTestIgnore("input_id_rpc", "input_content_rpc");

    OutputModelForTestIgnore output = service.test(input);

    TestMgr.check(null, output.getInputId());
    TestMgr.check(input.getContent(), output.getContent());
    TestMgr.check(null, output.getOutputId());
  }
}
