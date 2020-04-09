package org.apache.servicecomb.config.kie.client;

import java.util.Map;

import org.apache.servicecomb.config.kie.model.KVDoc;
import org.apache.servicecomb.config.kie.model.ValueType;
import org.junit.Assert;
import org.junit.Test;

public class TestKieUtil {
  @Test
  public void test_processValueType() {
    KVDoc kvDoc = new KVDoc();
    kvDoc.setKey("hello");
    kvDoc.setValue("world");
    Map<String, String> result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello"));

    kvDoc.setValueType("text");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello"));

    kvDoc.setValueType("string");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello"));

    kvDoc.setValueType("json");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello"));

    kvDoc.setValueType("yml");
    kvDoc.setValue("hello: world");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello.hello"));

    kvDoc.setValueType("yaml");
    kvDoc.setValue("hello: world");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello.hello"));

    kvDoc.setValueType("properties");
    kvDoc.setValue("hello=world");
    result = KieUtil.processValueType(kvDoc);
    Assert.assertEquals("world", result.get("hello.hello"));
  }
}
