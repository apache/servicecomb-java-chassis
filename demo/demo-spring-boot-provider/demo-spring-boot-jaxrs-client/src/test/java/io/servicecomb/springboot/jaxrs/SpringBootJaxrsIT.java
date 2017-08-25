package io.servicecomb.springboot.jaxrs;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.servicecomb.demo.TestMgr;
import io.servicecomb.springboot.jaxrs.client.JaxrsClient;

public class SpringBootJaxrsIT {

  @Before
  public void setUp() throws Exception {
    TestMgr.errors().clear();
  }

  @Test
  public void clientGetsNoError() throws Exception {
    JaxrsClient.main(new String[0]);

    assertThat(TestMgr.errors().isEmpty(), is(true));
  }
}
