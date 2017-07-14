package io.servicecomb.demo.pojo.test;

import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;

public class PojoTestMain {

  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();
  }
}