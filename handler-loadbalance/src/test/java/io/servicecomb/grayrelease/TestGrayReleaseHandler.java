package io.servicecomb.grayrelease;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Before;
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
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import io.servicecomb.swagger.invocation.AsyncResponse;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestGrayReleaseHandler {
    @Mocked
    private ServiceRegistryClient srClient;

    private AbstractServiceRegistry serviceRegistry;

    GrayReleaseHandler grayReleaseHandler = Mockito.mock(GrayReleaseHandler.class, Mockito.CALLS_REAL_METHODS);

    Invocation invocation = Mockito.mock(Invocation.class);

    @Before
    public void setup() {
        serviceRegistry = (AbstractServiceRegistry) ServiceRegistryFactory.createLocal();
        serviceRegistry.setServiceRegistryClient(srClient);
        serviceRegistry.getMicroserviceManager().addMicroservice("appId", "ms");
        serviceRegistry.init();

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

        System.out.println();
        System.out.println("-----------init--------------------------------------------------------");
        System.out.println(
                "instanceId:01" + "," + microserviceInstance01.getProperties().toString() + ",version={001;002}");
        System.out.println(
                "instanceId:02" + "," + microserviceInstance02.getProperties().toString() + ",version={001;002}");
        System.out.println(
                "instanceId:03" + "," + microserviceInstance03.getProperties().toString() + ",version={002}");
        System.out.println(
                "instanceId:04" + "," + microserviceInstance04.getProperties().toString() + ",version={002}");
        System.out.println();
        System.out.println("分组规则：");
        System.out.println("groupId:001,tag:001;002&&version:1.0.0.1");
        System.out.println("groupId:002,tag:002&&version:1.0.0.2");
        System.out.println("-----------end init--------------------------------------------------------");
        System.out.println();

        //参数tags,version
    }

    @Test
    public void testHandler() {
        AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
        try {
            String grayReleaseRuleClassName = "com.huawei.paas.cse.grayrelease.csefilter.GrayReleaseRatePolicyFilter";
            String rulePolicy = "[{\"group\":\"001\",\"type\":\"rate\",\"policy\":\"50\"}]";
            String groupPolicy =
                "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001,002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
            changeConfig(grayReleaseRuleClassName,
                    rulePolicy,
                    groupPolicy);

            Mockito.doNothing().when(invocation).next(asyncResp);
            System.out.println("-----------start--------------------------------------------------------");
            System.out.println();
            System.out.println("-----------rate方式--------------------------------------------------");
            System.out.println("规则：001:90,0001:10");
            System.out.println();

            InstanceCacheManager instanceCacheManager = serviceRegistry.getInstanceCacheManager();
            for (int i = 1; i < 11; i++) {
                System.out.println("-----------第" + i + "次-----------------------");
                System.out.println();
                grayReleaseHandler.handle(invocation, asyncResp);
                String key = Deencapsulation.invoke(instanceCacheManager,
                        "getKey",
                        invocation.getAppId(),
                        invocation.getMicroserviceName());

                Map<String, InstanceCache> cacheMap = Deencapsulation.getField(instanceCacheManager, "cacheMap");
                InstanceCache tmpInstanceCache = cacheMap.get(key);

                Map<String, MicroserviceInstance> tmpInstanceMap = Deencapsulation.getField(tmpInstanceCache,
                        "instanceMap");
                for (Map.Entry<String, MicroserviceInstance> instance : tmpInstanceMap.entrySet()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(instance.getKey()).append(":").append(instance.getValue().getProperties().toString());
                    System.out.println(sb);
                }
                System.out.println();
            }

            System.out.println();
            System.out.println("-----------策略方式--------------------------------------------------");
            System.out.println("规则：{0002:[{keyName:name,operator:like,value:w*d}]}");

            System.out.println("args:" + invocation.getArgs()[0]);

            grayReleaseRuleClassName = "com.huawei.paas.cse.grayrelease.csefilter.GrayReleaseRulePolicyFilter";
            rulePolicy = "[{\"group\":\"001\",\"type\":\"rule\",\"policy\":\"name~w*d\"},"
                    + "{\"group\":\"002\",\"type\":\"rule\",\"policy\":\"name~user\"}]";
            groupPolicy =
                "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001,002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
            changeConfig(grayReleaseRuleClassName,
                    rulePolicy,
                    groupPolicy);

            System.out.println();
            for (int i = 1; i < 4; i++) {
                System.out.println("-----------第" + i + "次-----------------------");
                System.out.println();
                grayReleaseHandler.handle(invocation, asyncResp);
                String key = Deencapsulation.invoke(instanceCacheManager,
                        "getKey",
                        invocation.getAppId(),
                        invocation.getMicroserviceName());

                Map<String, InstanceCache> cacheMap = Deencapsulation.getField(instanceCacheManager, "cacheMap");
                InstanceCache tmpInstanceCache = cacheMap.get(key);

                Map<String, MicroserviceInstance> tmpInstanceMap = Deencapsulation.getField(tmpInstanceCache,
                        "instanceMap");
                for (Map.Entry<String, MicroserviceInstance> instance : tmpInstanceMap.entrySet()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(instance.getKey()).append(":").append(instance.getValue().getProperties().toString());
                    System.out.println(sb);
                }
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println(e);
        }

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
