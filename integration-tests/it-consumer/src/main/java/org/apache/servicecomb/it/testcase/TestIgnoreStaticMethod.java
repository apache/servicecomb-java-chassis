package org.apache.servicecomb.it.testcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.junit.Before;
import org.junit.Test;

public class TestIgnoreStaticMethod {
  interface IgnoreStaticMethod {
    int sub(int num1, int num2);

    int add(int num1, int num2);
  }

  private static Consumers<IgnoreStaticMethod> consumers;

  private static String producerName;

  @Before
  public void prepare() {
    if (!ITJUnitUtils.getProducerName().equals(producerName)) {
      producerName = ITJUnitUtils.getProducerName();
      consumers = new Consumers<>(producerName, "ignoreStaticMethodSchema", IgnoreStaticMethod.class);
      consumers.init(ITJUnitUtils.getTransport());
    }
  }



  @Test
  public void ignoreStaticMethod_pojo() {

    int add = consumers.getIntf().add(5, 8);
    assertEquals(13, add);
    try {
      int sub = consumers.getIntf().sub(5, 8);
    } catch (Exception exception) {
      assertTrue(exception.getMessage().contains("sub not exist"));
    }

  }

  @Test
  public void ignoreStaticMethod_rt() {
    Map<String, Integer> map = new HashMap<>();
    map.put("num1", 5);
    map.put("num2", 8);
    int add = consumers.getSCBRestTemplate().postForObject("/add", map, int.class);
    assertEquals(13, add);

    try {
      int sub = consumers.getSCBRestTemplate().postForObject("/sub", map, int.class);
    } catch (Exception exception) {
      assertTrue(exception.getMessage().contains("message=Not Found"));
    }
  }
}
