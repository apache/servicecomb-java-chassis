package io.servicecomb.demo.pojo;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.pojo.client.PojoClient;

public class PojoIT {

  @Before
  public void setUp() throws Exception {
    TestMgr.errors().clear();
  }

  @Test
  public void clientGetsNoError() throws Exception {
    PojoClient.main(new String[0]);

    assertThat(TestMgr.errors().isEmpty(), is(true));
  }
}
