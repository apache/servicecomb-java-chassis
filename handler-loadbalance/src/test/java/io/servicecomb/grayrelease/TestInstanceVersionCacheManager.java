package io.servicecomb.grayrelease;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.InstanceVersionCacheManager;
import mockit.Deencapsulation;

@RunWith(PowerMockRunner.class)
public class TestInstanceVersionCacheManager {

    InstanceVersionCacheManager instanceVersionCacheManager = InstanceVersionCacheManager.INSTANCE;

    private final String VERSIONALL = "0+";

    private String appId = "testAppId";

    private String microserviceName = "testMicroserviceName";

    {
        init();
    }

    public void init() {
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

/*    	MicroserviceInstance ins1 = new MicroserviceInstance();
    	ins1.setServiceId("001");
    	ins1.setInstanceId("01");
    	MicroserviceInstance ins2 = new MicroserviceInstance();
    	ins1.setServiceId("002");
    	ins1.setInstanceId("02");
    	MicroserviceInstance ins3 = new MicroserviceInstance();
    	ins1.setServiceId("003");
    	ins1.setInstanceId("03");
    	MicroserviceInstance ins4 = new MicroserviceInstance();
    	ins1.setServiceId("004");
    	ins1.setInstanceId("04");   */ 	
        List<MicroserviceInstance> instances = new ArrayList<MicroserviceInstance>();
        instances.add(ins1);
        instances.add(ins2);
        instances.add(ins3);
        instances.add(ins4);
        List<MicroserviceInstance> instances2 = new ArrayList<MicroserviceInstance>();
        instances2.add(ins3);
        instances2.add(ins4);


        PowerMockito.mockStatic(RegistryUtils.class);
        PowerMockito.when(RegistryUtils.findServiceInstance(appId, microserviceName, VERSIONALL))
                .thenReturn(instances);
        PowerMockito.when(RegistryUtils.findServiceInstance(appId, microserviceName, "1.0.0.2+"))
                .thenReturn(instances2);
        Microservice microservice1 = Mockito.mock(Microservice.class);
        Mockito.when(microservice1.getVersion()).thenReturn("1.0.0.1");
        Microservice microservice2 = Mockito.mock(Microservice.class);
        Mockito.when(microservice2.getVersion()).thenReturn("1.0.0.1");
        Microservice microservice3 = Mockito.mock(Microservice.class);
        Mockito.when(microservice3.getVersion()).thenReturn("1.0.0.2");
        Microservice microservice4 = Mockito.mock(Microservice.class);
        Mockito.when(microservice4.getVersion()).thenReturn("1.0.0.2");        
        PowerMockito.when(RegistryUtils.getMicroservice("001")).thenReturn(microservice1);
        PowerMockito.when(RegistryUtils.getMicroservice("002")).thenReturn(microservice2);
        PowerMockito.when(RegistryUtils.getMicroservice("003")).thenReturn(microservice3);
        PowerMockito.when(RegistryUtils.getMicroservice("004")).thenReturn(microservice4);
    }

    @Test
    @PrepareForTest(RegistryUtils.class)
    public void testGetOrCreateAllMapAllVersion() {

        instanceVersionCacheManager.getOrCreateAllMap(appId, microserviceName);
        Map<String, Map<String, Map<String, MicroserviceInstance>>> cacheAllMap =
            Deencapsulation.getField(instanceVersionCacheManager, "cacheAllMap");
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
    @PrepareForTest(RegistryUtils.class)
    public void testGetOrCreateAllMapWithVersion() {
        Map<String, MicroserviceInstance> vinstances =
            instanceVersionCacheManager.getOrCreateAllMap(appId, microserviceName, "1.0.0.1");
        System.out.println(vinstances);
        Assert.assertNotNull(vinstances.get("01"));
        Assert.assertNotNull(vinstances.get("02"));
        Assert.assertEquals("01",vinstances.get("01").getInstanceId());
        Assert.assertEquals("02",vinstances.get("02").getInstanceId());          
    }

    @Test
    @PrepareForTest(RegistryUtils.class)
    public void testGetOrCreateVRuleMap() {
        Map<String, Map<String, MicroserviceInstance>> vRuleIns =
            instanceVersionCacheManager.getOrCreateVRuleMap(appId, microserviceName, "1.0.0.2+");
        System.out.println("VRuleIns:" + vRuleIns);
        Assert.assertNotNull(vRuleIns.get("1.0.0.2"));
        Assert.assertNotNull(vRuleIns.get("1.0.0.2").get("03"));
        Assert.assertNotNull(vRuleIns.get("1.0.0.2").get("04"));
        Assert.assertEquals("03",vRuleIns.get("1.0.0.2").get("03").getInstanceId());
        Assert.assertEquals("04",vRuleIns.get("1.0.0.2").get("04").getInstanceId());         
    }

}
