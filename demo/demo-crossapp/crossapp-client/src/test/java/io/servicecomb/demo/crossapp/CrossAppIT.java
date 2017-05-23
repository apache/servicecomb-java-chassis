package io.servicecomb.demo.crossapp;

import io.servicecomb.demo.TestMgr;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CrossAppIT {

    @Before
    public void setUp() throws Exception {
        TestMgr.errors().clear();
    }

    @Test
    public void clientGetsNoError() throws Exception {
        CrossappClient.main(new String[0]);

        assertThat(TestMgr.errors().isEmpty(), is(true));
    }
}
