package io.servicecomb.demo.discovery.client;

import org.junit.Before;
import org.junit.Test;
import io.servicecomb.demo.TestMgr;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DiscoveryClientIT {

	@Before
	public void setUp() {
		TestMgr.errors().clear();
	}

	@Test
	public void clientGetsNoError() throws Exception {
		DiscoveryClient.main(new String[0]);

		assertThat(TestMgr.errors().isEmpty(), is(true));
	}

}
