package io.servicecomb.demo.discovery.client;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.servicecomb.demo.TestMgr;

public class DiscoveryClientIT {

	@Before
	public void setUp() {
		TestMgr.errors().clear();
	}

	@Test
	public void clientGetsNoError() throws Exception {
		DiscoveryClient.main(new String[0]);
		System.out.println(TestMgr.errors().toString());
		assertThat(TestMgr.errors().isEmpty(), is(true));
	}

}
