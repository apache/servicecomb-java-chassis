package io.servicecomb.grayrelease;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestInstanceVersionCacheManager {
    @Mocked
    private ServiceRegistryClient srClient;

    private AbstractServiceRegistry serviceRegistry;

    private final String VERSIONALL = "0+";

    private String appId = "testAppId";

    private String microserviceName = "testMicroserviceName";

    private Microservice microservice1;

    private Microservice microservice2;

    private Microservice microservice3;

    private Microservice microservice4;

    private List<MicroserviceInstance> instances;

    private List<MicroserviceInstance> instances2;

    @Before
    public void setup() {
        MicroserviceInstance ins1 = Mockito.mock(MicroserviceInstance.class);
        MicroserviceInstance ins2 = Mockito.mock(MicroserviceInstance.class);
        MicroserviceInstance ins3 = Mockito.mock(MicroserviceInstance.class);
        MicroserviceInstance ins4 = Mockito.mock(MicroserviceInstance.class);

        Mockito.when(ins1.getServiceId()).thenReturn("001");
        Mockito.when(ins1.getInstanceId()).thenReturn("01");
        Mockito.when(ins2.getServiceId()).thenReturn("002");
        Mockito.when(ins2.getInstanceId()).thenReturn("02");
        Mockito.when(ins3.getServiceId()).thenReturn("003");
        Mockito.when(ins3.getInstanceId()).thenReturn("03");
        Mockito.when(ins4.getServiceId()).thenReturn("004");
        Mockito.when(ins4.getInstanceId()).thenReturn("04");

        instances = new ArrayList<MicroserviceInstance>();

        instances.add(ins1);
        instances.add(ins2);
        instances.add(ins3);
        instances.add(ins4);



        microservice1 = Mockito.mock(Microservice.class);
        Mockito.when(microservice1.getVersion()).thenReturn("1.0.0.1");
        microservice2 = Mockito.mock(Microservice.class);
        Mockito.when(microservice2.getVersion()).thenReturn("1.0.0.1");
        microservice3 = Mockito.mock(Microservice.class);
        Mockito.when(microservice3.getVersion()).thenReturn("1.0.0.2");
        microservice4 = Mockito.mock(Microservice.class);
        Mockito.when(microservice4.getVersion()).thenReturn("1.0.0.2");

        instances2 = new ArrayList<MicroserviceInstance>();
        instances2.add(ins3);
        instances2.add(ins4);

        serviceRegistry = (AbstractServiceRegistry) ServiceRegistryFactory.createLocal();
        serviceRegistry.setServiceRegistryClient(srClient);
        serviceRegistry.getMicroserviceManager().addMicroservice(appId, "ms");
        serviceRegistry.init();

    }

    @Test
    public void testGetOrCreateAllMapAllVersion() {
        new Expectations() {
            {
                srClient.findServiceInstance(anyString, appId, microserviceName, VERSIONALL);
                result = instances;
                srClient.getMicroservice("001");
                result = microservice1;
                srClient.getMicroservice("002");
                result = microservice2;
                srClient.getMicroservice("003");
                result = microservice3;
                srClient.getMicroservice("004");
                result = microservice4;
            }
        };

        serviceRegistry.getInstanceVersionCacheManager().getOrCreateAllMap(appId, microserviceName);
        Map<String, Map<String, Map<String, MicroserviceInstance>>> cacheAllMap =
            Deencapsulation.getField(serviceRegistry.getInstanceVersionCacheManager(), "cacheAllMap");
        System.out.println(cacheAllMap);

        String key = "testAppId/testMicroserviceName";
        Map<String, Map<String, MicroserviceInstance>> inVs = cacheAllMap.get(key);
        Assert.assertNotNull(inVs.get("1.0.0.1"));
        Map<String, MicroserviceInstance> ins = inVs.get("1.0.0.1");
        Assert.assertNotNull(ins.get("01"));
        Assert.assertNotNull(ins.get("02"));
        Assert.assertEquals("01",ins.get("01").getInstanceId());
        Assert.assertEquals("02",ins.get("02").getInstanceId());        
        
        Map<String, MicroserviceInstance> ins002 = inVs.get("1.0.0.2");
        Assert.assertNotNull(ins002.get("03"));
        Assert.assertNotNull(ins002.get("04"));
        Assert.assertEquals("03",ins002.get("03").getInstanceId());
        Assert.assertEquals("04",ins002.get("04").getInstanceId());         
    }

    @Test
    public void testGetOrCreateAllMapWithVersion() {
        new Expectations() {
            {
                srClient.findServiceInstance(anyString, appId, microserviceName, VERSIONALL);
                result = instances;
                srClient.getMicroservice("001");
                result = microservice1;
                srClient.getMicroservice("002");
                result = microservice2;
                srClient.getMicroservice("003");
                result = microservice3;
                srClient.getMicroservice("004");
                result = microservice4;
            }
        };

        Map<String, MicroserviceInstance> vinstances =
            serviceRegistry.getInstanceVersionCacheManager().getOrCreateAllMap(appId, microserviceName, "1.0.0.1");
        System.out.println(vinstances);
        Assert.assertNotNull(vinstances.get("01"));
        Assert.assertNotNull(vinstances.get("02"));
        Assert.assertEquals("01",vinstances.get("01").getInstanceId());
        Assert.assertEquals("02",vinstances.get("02").getInstanceId());          
    }

    @Test
    public void testGetOrCreateVRuleMap() {
        new Expectations() {
            {
                srClient.findServiceInstance(anyString, appId, microserviceName, "1.0.0.2+");
                result = instances2;
                srClient.getMicroservice("003");
                result = microservice3;
                srClient.getMicroservice("004");
                result = microservice4;
            }
        };

        Map<String, Map<String, MicroserviceInstance>> vRuleIns =
            serviceRegistry.getInstanceVersionCacheManager().getOrCreateVRuleMap(appId, microserviceName, "1.0.0.2+");
        System.out.println("VRuleIns:" + vRuleIns);
        Assert.assertNotNull(vRuleIns.get("1.0.0.2"));
        Assert.assertNotNull(vRuleIns.get("1.0.0.2").get("03"));
        Assert.assertNotNull(vRuleIns.get("1.0.0.2").get("04"));
        Assert.assertEquals("03",vRuleIns.get("1.0.0.2").get("03").getInstanceId());
        Assert.assertEquals("04",vRuleIns.get("1.0.0.2").get("04").getInstanceId());         
    }

}
