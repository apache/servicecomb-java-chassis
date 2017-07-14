package io.servicecomb.demo.pojo.test;

import org.junit.BeforeClass;

public class RawPojoIntegrationTest extends PojoIntegrationTestBase {

  @BeforeClass
  public static void setUp() throws Exception {
    PojoTestMain.main(new String[0]);
  }
}