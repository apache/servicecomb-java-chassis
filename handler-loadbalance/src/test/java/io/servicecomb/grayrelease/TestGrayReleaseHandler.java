package io.servicecomb.grayrelease;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.ConfigurationBackedDynamicPropertySupportImpl;
import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.InstanceCache;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import io.servicecomb.serviceregistry.cache.InstanceVersionCacheManager;
import io.servicecomb.swagger.invocation.AsyncResponse;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestGrayReleaseHandler {

    GrayReleaseHandler grayReleaseHandler = Mockito.mock(GrayReleaseHandler.class, Mockito.CALLS_REAL_METHODS);

    Invocation invocation = Mockito.mock(Invocation.class);

    {
        this.initInvocation();
        this.initInstance();
    }

    public void initInvocation() {

        OperationMeta opMeta = Mockito.mock(OperationMeta.class);
        Mockito.when(opMeta.getParamSize()).thenReturn(1);
        Mockito.when(opMeta.getParamName(0)).thenReturn("name");
        Mockito.when(invocation.getOperationMeta()).thenReturn(opMeta);
        Object[] arg = new Object[1];
        arg[0] = "world";
        Mockito.when(invocation.getArgs()).thenReturn(arg);
        Mockito.when(invocation.getAppId()).thenReturn("testApp");
        Mockito.when(invocation.getMicroserviceName()).thenReturn("testMicorserverName");
        Mockito.when(invocation.getMicroserviceVersionRule()).thenReturn("testMicroserviceVersionRule");
        Mockito.when(invocation.getInvocationQualifiedName()).thenReturn("testInvocationQualifiedName");

    }

    public void initInstance() {
        Map<String, Map<String, MicroserviceInstance>> versionInstanceMap =
            new HashMap<String, Map<String, MicroserviceInstance>>();
        Map<String, MicroserviceInstance> ins01 = new HashMap<String, MicroserviceInstance>();
        MicroserviceInstance microserviceInstance01 = Mockito.mock(MicroserviceInstance.class);
        MicroserviceInstance microserviceInstance02 = Mockito.mock(MicroserviceInstance.class);
        Map<String, String> props001 = new HashMap<String, String>();
        props001.put("tags", "001;002");
        Mockito.when(microserviceInstance01.getInstanceId()).thenReturn("01");
        Mockito.when(microserviceInstance02.getInstanceId()).thenReturn("02");        
        Mockito.when(microserviceInstance01.getProperties()).thenReturn(props001);
        Mockito.when(microserviceInstance02.getProperties()).thenReturn(props001);
        ins01.put("01", microserviceInstance01);
        ins01.put("02", microserviceInstance02);
        versionInstanceMap.put("1.0.0.1", ins01);
        Map<String, MicroserviceInstance> ins02 = new HashMap<String, MicroserviceInstance>();
        MicroserviceInstance microserviceInstance03 = Mockito.mock(MicroserviceInstance.class);
        MicroserviceInstance microserviceInstance04 = Mockito.mock(MicroserviceInstance.class);
        Map<String, String> props002 = new HashMap<String, String>();
        props002.put("tags", "002");
        Mockito.when(microserviceInstance03.getInstanceId()).thenReturn("03");
        Mockito.when(microserviceInstance04.getInstanceId()).thenReturn("04");  
        Mockito.when(microserviceInstance03.getProperties()).thenReturn(props002);
        Mockito.when(microserviceInstance04.getProperties()).thenReturn(props002);
        ins02.put("03", microserviceInstance03);
        ins02.put("04", microserviceInstance04);
        versionInstanceMap.put("1.0.0.2", ins02);

        new MockUp<InstanceVersionCacheManager>() {
            @Mock
            public Map<String, Map<String, MicroserviceInstance>> getOrCreateAllMap(String appId,
                    String microserviceName) {
                return versionInstanceMap;
            }

            @Mock
            public Map<String, Map<String, MicroserviceInstance>> getOrCreateVRuleMap(String appId,
                    String microserviceName,
                    String microserviceVersionRule) {
                return versionInstanceMap;
            }

        };
    }

    @Test
    public void testHandler() {
    	boolean isSuccess=true;
		AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
		try {
			String grayReleaseRuleClassName = "io.servicecomb.grayrelease.csefilter.GrayReleaseRatePolicyFilter";
			String rulePolicy = "[{\"group\":\"001\",\"type\":\"rate\",\"policy\":\"100\"}]";
			String groupPolicy = "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001,002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
			changeConfig(grayReleaseRuleClassName, rulePolicy, groupPolicy);

			Mockito.doNothing().when(invocation).next(asyncResp);

			for (int i = 1; i < 11; i++) {
				grayReleaseHandler.handle(invocation, asyncResp);
				InstanceCacheManager instanceCacheManager = InstanceCacheManager.INSTANCE;
				String key = Deencapsulation.invoke(instanceCacheManager, "getKey", invocation.getAppId(),
						invocation.getMicroserviceName());

				Map<String, InstanceCache> cacheMap = Deencapsulation.getField(instanceCacheManager, "cacheMap");
				InstanceCache tmpInstanceCache = cacheMap.get(key);

				Map<String, MicroserviceInstance> tmpInstanceMap = Deencapsulation.getField(tmpInstanceCache,
						"instanceMap");
				Assert.assertNotNull(tmpInstanceMap.get("01"));
				Assert.assertNotNull(tmpInstanceMap.get("02"));				
				for (Map.Entry<String, MicroserviceInstance> instance : tmpInstanceMap.entrySet()) {
					Assert.assertTrue("01".equals(instance.getKey()) || "02".equals(instance.getKey()));
					Assert.assertTrue("01".equals(instance.getValue().getInstanceId()) || "02".equals(instance.getKey()));
				}
			}

			grayReleaseRuleClassName = "io.servicecomb.grayrelease.csefilter.GrayReleaseRulePolicyFilter";
			rulePolicy = "[{\"group\":\"001\",\"type\":\"rule\",\"policy\":\"name~w*d\"},"
					+ "{\"group\":\"002\",\"type\":\"rule\",\"policy\":\"name~user\"}]";
			groupPolicy = "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001,002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
			changeConfig(grayReleaseRuleClassName, rulePolicy, groupPolicy);

			for (int i = 1; i < 4; i++) {
				grayReleaseHandler.handle(invocation, asyncResp);
				InstanceCacheManager instanceCacheManager = InstanceCacheManager.INSTANCE;
				String key = Deencapsulation.invoke(instanceCacheManager, "getKey", invocation.getAppId(),
						invocation.getMicroserviceName());

				Map<String, InstanceCache> cacheMap = Deencapsulation.getField(instanceCacheManager, "cacheMap");
				InstanceCache tmpInstanceCache = cacheMap.get(key);

				Map<String, MicroserviceInstance> tmpInstanceMap = Deencapsulation.getField(tmpInstanceCache,
						"instanceMap");
				Assert.assertNotNull(tmpInstanceMap.get("01"));
				Assert.assertNotNull(tmpInstanceMap.get("02"));						
				for (Map.Entry<String, MicroserviceInstance> instance : tmpInstanceMap.entrySet()) {
					Assert.assertTrue("01".equals(instance.getKey()) || "02".equals(instance.getKey()));
					Assert.assertTrue("01".equals(instance.getValue().getInstanceId()) || "02".equals(instance.getKey()));
				}
			}

		} catch (Exception e) {
			isSuccess=false;
		}
		Assert.assertTrue(isSuccess);

    }

    public void changeConfig(String grayReleaseRuleClassName,
            String rulePolicy, String groupPolicy) {
        if (DynamicPropertyFactory.getBackingConfigurationSource() == null) {
            AbstractConfiguration configuration = new BaseConfiguration();
            DynamicPropertyFactory
                    .initWithConfigurationSource(new ConfigurationBackedDynamicPropertySupportImpl(configuration));
            configuration.addProperty("cse.grayrelease.testMicorserverName.GrayReleaseRuleClassName",
                    grayReleaseRuleClassName);
            configuration.addProperty("cse.grayrelease.testMicorserverName.rule.policy", rulePolicy);
            configuration.addProperty("cse.grayrelease.testMicorserverName.group.policy", groupPolicy);
        } else {
            AbstractConfiguration configuration =
                (AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource();
            configuration.setProperty("cse.grayrelease.testMicorserverName.GrayReleaseRuleClassName",
                    grayReleaseRuleClassName);
            configuration.setProperty("cse.grayrelease.testMicorserverName.rule.policy", rulePolicy);
            configuration.setProperty("cse.grayrelease.testMicorserverName.group.policy", groupPolicy);
        }

    }

}
