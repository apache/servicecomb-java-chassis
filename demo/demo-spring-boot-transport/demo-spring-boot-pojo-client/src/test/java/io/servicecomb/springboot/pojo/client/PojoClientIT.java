package io.servicecomb.springboot.pojo.client;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;

import io.servicecomb.demo.TestMgr;

public class PojoClientIT {

  @Before
  public void setUp() throws Exception {
    TestMgr.errors().clear();
  }

  @Test
  public void clientGetsNoError() throws Exception {
    SpringApplication.run(PojoClient.class);

    assertThat(TestMgr.errors().isEmpty(), is(true));
  }
}
