package io.servicecomb.demo.ignore;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.servicecomb.demo.TestMgr;

public class IgnoreIT {
  @Before
  public void setUp() throws Exception {
    TestMgr.errors().clear();
  }

  @Test
  public void clientGetsNoError() throws Exception {
    AllTestClient.main(new String[0]);

    assertThat(TestMgr.errors().isEmpty(), is(true));
  }
}
