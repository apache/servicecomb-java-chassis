package org.apache.servicecomb.it.testcase;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.junit.Assert;
import org.junit.Test;

public class TestIgnoreStaticMethod {
  private MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();

  @Test
  public void ignoreStaticMethod_pojo() {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta("ignoreStaticMethodPojoSchema");
    OperationMeta add = schemaMeta.findOperation("add");
    Assert.assertNotNull(add);

    OperationMeta sub = schemaMeta.findOperation("staticSub");
    Assert.assertNull(sub);
  }


  @Test
  public void ignoreStaticMethod_Jaxrs() {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta("ignoreStaticMethodJaxrsSchema");
    OperationMeta add = schemaMeta.findOperation("add");
    Assert.assertNotNull(add);

    OperationMeta sub = schemaMeta.findOperation("staticSub");
    Assert.assertNull(sub);
  }

  @Test
  public void ignoreStaticMethod_Springmvc() {
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta("ignoreStaticMethodSpringmvcSchema");
    OperationMeta add = schemaMeta.findOperation("add");
    Assert.assertNotNull(add);

    OperationMeta sub = schemaMeta.findOperation("staticSub");
    Assert.assertNull(sub);
  }
}
