package io.servicecomb.demo.pojo;

import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.pojo.client.PojoClient;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
