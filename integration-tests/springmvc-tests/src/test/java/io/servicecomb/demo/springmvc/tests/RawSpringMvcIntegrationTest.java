package io.servicecomb.demo.springmvc.tests;

import org.junit.BeforeClass;

public class RawSpringMvcIntegrationTest extends SpringMvcIntegrationTestBase {

  @BeforeClass
  public static void setUp() throws Exception {
    SpringMvcTestMain.main(new String[0]);
  }

}