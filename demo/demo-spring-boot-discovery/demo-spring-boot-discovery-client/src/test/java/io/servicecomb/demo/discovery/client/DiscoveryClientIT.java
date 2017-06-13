package io.servicecomb.demo.discovery.client;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.springmvc.server.SpringmvcServer;

public class DiscoveryClientIT {

	@Before
	public void setUp() {
		TestMgr.errors().clear();
	}

	@Test
	public void clientGetsNoError() throws Exception {
		SpringmvcServer.main(new String[0]);
		DiscoveryClient.main(new String[0]);
		assertThat(TestMgr.errors().isEmpty(), is(true));
	}

}
