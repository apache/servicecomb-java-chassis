package io.servicecomb.loadbalance.filter;

import com.netflix.loadbalancer.Server;
import io.servicecomb.loadbalance.CseServer;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestStageDistinguishFilter {

    StageDistinguishFilter stageDistinguishFilter = new StageDistinguishFilter();

    MicroserviceInstance microserviceInstance = Mockito.mock(MicroserviceInstance.class);

    @Before
    public void setUp() throws Exception {
        Mockito.when(microserviceInstance.getEnvironment()).thenReturn("production");
        Mockito.when(microserviceInstance.getStatus()).thenReturn(MicroserviceInstanceStatus.UP);
        new MockUp<RegistryUtils>() {
            @Mock
            public MicroserviceInstance getMicroserviceInstance() {
                return microserviceInstance;
            }
        };
    }

    @Test
    public void getFilteredListOfServers_Match() throws Exception {
        CseServer testServer = Mockito.mock(CseServer.class);


        Mockito.when(testServer.getInstance()).thenReturn(microserviceInstance);



        List<Server> serverList = new ArrayList<Server>();
        serverList.add(testServer);
        List<Server> returnedServerList = stageDistinguishFilter.getFilteredListOfServers(serverList);
        Assert.assertEquals(returnedServerList.size(), 1);
    }


    @Test
    public void getFilteredListOfServers_Not_Match() throws Exception {
        CseServer testServer = Mockito.mock(CseServer.class);
        MicroserviceInstance microserviceInstance1 = Mockito.mock(MicroserviceInstance.class);
        Mockito.when(microserviceInstance1.getEnvironment()).thenReturn("XXX");
        Mockito.when(microserviceInstance1.getStatus()).thenReturn(MicroserviceInstanceStatus.UP);
        Mockito.when(testServer.getInstance()).thenReturn(microserviceInstance1);

        CseServer testServer1 = Mockito.mock(CseServer.class);
        MicroserviceInstance microserviceInstance2 = Mockito.mock(MicroserviceInstance.class);
        Mockito.when(microserviceInstance2.getEnvironment()).thenReturn("production");
        Mockito.when(microserviceInstance2.getStatus()).thenReturn(MicroserviceInstanceStatus.DOWN);
        Mockito.when(testServer1.getInstance()).thenReturn(microserviceInstance2);

        List<Server> serverList = new ArrayList<Server>();
        serverList.add(testServer);
        serverList.add(testServer1);

        new MockUp<RegistryUtils>() {
            @Mock
            public MicroserviceInstance getMicroserviceInstance() {
                return microserviceInstance;
            }
        };


        List<Server> returnedServerList = stageDistinguishFilter.getFilteredListOfServers(serverList);
        Assert.assertEquals(returnedServerList.size(), 0);
    }

}