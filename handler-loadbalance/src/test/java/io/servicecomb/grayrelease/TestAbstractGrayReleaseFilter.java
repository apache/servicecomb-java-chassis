package io.servicecomb.grayrelease;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.grayrelease.AbstractGrayReleaseFilter.InstanceScope;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import io.servicecomb.serviceregistry.cache.InstanceVersionCacheManager;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

//@RunWith(PowerMockRunner.class)
public class TestAbstractGrayReleaseFilter {

    private AbstractGrayReleaseFilter filter =
        Mockito.mock(AbstractGrayReleaseFilter.class, Mockito.CALLS_REAL_METHODS);

    Invocation invocation = Mockito.mock(Invocation.class);

    @Before
    public void setup() {
        initInvocation();
        initCache();
    }

    public void initCache() {
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

        InstanceVersionCacheManager instanceVersionCacheManager = new MockUp<InstanceVersionCacheManager>() {
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

        }.getMockInstance();

        new MockUp<RegistryUtils>() {
            @Mock
            InstanceVersionCacheManager getInstanceVersionCacheManager() {
                return instanceVersionCacheManager;
            }

            @Mock
            InstanceCacheManager getInstanceCacheManager() {
                return Mockito.mock(InstanceCacheManager.class);
            }
        };

        //参数tags,version
        Mockito.when(filter.getGroupNameByGroupRule("001;002", "1.0.0.1")).thenReturn("001");
        Mockito.when(filter.getGroupNameByGroupRule("002", "1.0.0.2")).thenReturn("002");

    }

    public void initInvocation() {
        OperationMeta opMeta = Mockito.mock(OperationMeta.class);
        Mockito.when(opMeta.getParamSize()).thenReturn(1);
        Mockito.when(opMeta.getParamName(0)).thenReturn("name");
        Mockito.when(invocation.getOperationMeta()).thenReturn(opMeta);
        Object[] arg = new Object[1];
        arg[0] = "world";
        Mockito.when(invocation.getArgs()).thenReturn(arg);
        System.out.println(invocation.getOperationMeta().getParamSize());
        System.out.println(invocation.getOperationMeta().getParamName(0));
        Mockito.when(invocation.getAppId()).thenReturn("testApp");
        Mockito.when(invocation.getMicroserviceName()).thenReturn("testMicorserverName");
        Mockito.when(invocation.getMicroserviceVersionRule()).thenReturn("testMicroserviceVersionRule");
        Deencapsulation.setField(filter, "invocation", invocation);
    }

    @Test
    public void testInit() {
        boolean status = true;
        Invocation invo = Mockito.mock(Invocation.class);
        try {
            System.out.println();
            filter.init(invo);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testFilterRule() {

        boolean status = true;
        Mockito.doNothing().when(filter).fillGroupRules();
        Mockito.doNothing().when(filter).fillGrayRules();;
        Mockito.when(filter.isReqCompare()).thenReturn(true);
        new MockUp<AbstractGrayReleaseFilter>() {
            @Mock
            private void getReqParams() {
                return;
            }
        };
        Mockito.when(filter.getGroupNameByGroupRule(null, null)).thenReturn("0002");
        Mockito.doNothing().when(filter).fillGroupRules();
        Mockito.doNothing().when(filter).updateInstanceCache("0002");
        try {
            filter.filterRule();
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGetReqParams() {

        boolean status = true;
        Deencapsulation.invoke(filter, "fillReqParams");
        Map<String, Object> filters = filter.reqParams;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "world");
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String key = filter.getKey();
            String value = (String) filter.getValue();
            if (map.get(key) == null || !value.equals(map.get(key))) {
                status = false;
                break;
            }
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testFillInstanceGroup() {
        try {
            Deencapsulation.setField(filter, "instanceScope", InstanceScope.All);
            filter.fillInstanceGroup();
            filter.updateInstanceCache("test001");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
