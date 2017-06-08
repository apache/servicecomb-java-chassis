package io.servicecomb.grayrelease.csefilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.ConfigurationBackedDynamicPropertySupportImpl;
import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.grayrelease.AbstractGrayReleaseFilter.InstanceScope;
import io.servicecomb.grayrelease.GrayReleaseGroupPolicy;
import io.servicecomb.grayrelease.GrayReleaseRulePolicy;
import io.servicecomb.grayrelease.csefilter.AbstractCseRuleGrayReleaseFilter.RelateSymbol;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.InstanceVersionCacheManager;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestAbstractCseRuleGrayReleaseFilter {

    private AbstractCseRuleGrayReleaseFilter filter = Mockito.mock(AbstractCseRuleGrayReleaseFilter.class,
            Mockito.CALLS_REAL_METHODS);

    {
        this.initInvocation();
        this.initCache();
    }

    /**
     * 
     * instances:01、02、03、04
     * version:[{version:1.0.0.1,instances:[{01,tags=001;002},{02,tags=001;002}}{version:1.0.0.2,instances:[{03,tags=002},{04,tags=002}}]
     * groups:[{group:001,tags:001;002,version:1.0.0.1},{group:002,tags:002,version:1.0.0.2}]
     */
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

        //参数tags,version
        /*        Mockito.when(filter.getGroupNameByGroupRule("001;002", "1.0.0.1")).thenReturn("001");
        Mockito.when(filter.getGroupNameByGroupRule("002", "1.0.0.2")).thenReturn("002");*/
    }

    @BeforeClass
    public static void beforeCls() {
        if (DynamicPropertyFactory.getBackingConfigurationSource() == null) {
            AbstractConfiguration configuration = new BaseConfiguration();
            DynamicPropertyFactory
                    .initWithConfigurationSource(new ConfigurationBackedDynamicPropertySupportImpl(configuration));
            configuration.addProperty("cse.grayrelease.testMicorserverName.GrayReleaseRuleClassName",
                    "com.huawei.paas.cse.grayrelease.csefilter.GrayReleaseRatePolicyFilter");
            configuration.addProperty("cse.grayrelease.testMicorserverName.rule.policy",
                    "[{\"group\":\"group1\",\"type\":\"rate\",\"policy\":\"20%\"}]");
            configuration.addProperty("cse.grayrelease.testMicorserverName.group.policy",
                    "[{\"name\":\"group1\",\"rule\":\"version=0.0.3||tags=1;2;3\"},{\"name\":\"group2\",\"rule\":\"version=1.0.0.2\"}]");
        }
    }

    public void initInvocation() {

        Invocation invocation = Mockito.mock(Invocation.class);
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
        Mockito.when(filter.getInvocation()).thenReturn(invocation);
        Deencapsulation.setField(filter, "invocation", invocation);

    }

    @Test
    public void testFillGrayRules() {

        boolean status = true;
        try {
            String grayReleaseRuleClassName = "com.huawei.paas.cse.grayrelease.csefilter.GrayReleaseRatePolicyFilter";
            String rulePolicy = "[{\"group\":\"001\",\"type\":\"rate\",\"policy\":\"20\"}]";
            String groupPolicy =
                "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001;002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
            changeConfig(grayReleaseRuleClassName,
                    rulePolicy,
                    groupPolicy);
            filter.fillGrayRules();
            Deencapsulation.invoke(filter, "fillGrayRules");
            String sruleStr = "[{\"groupName\":\"001\",\"type\":\"rate\",\"policy\":\"20\"}]";
            List<GrayReleaseRulePolicy> fis = filter.getGrayRules();
            String mrulStr = JsonUtils.writeValueAsString(fis);
            Assert.assertEquals(sruleStr, mrulStr);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testFillGroupRules() {

        boolean status = true;
        try {
            String grayReleaseRuleClassName = "com.huawei.paas.cse.grayrelease.csefilter.GrayReleaseRatePolicyFilter";
            String rulePolicy = "[{\"group\":\"001\",\"type\":\"rate\",\"policy\":\"20\"}]";
            String groupPolicy =
                "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001;002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
            changeConfig(grayReleaseRuleClassName,
                    rulePolicy,
                    groupPolicy);
            filter.fillGroupRules();
            String sruleStr =
                "[{\"groupName\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001;002\"},{\"groupName\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
            List<GrayReleaseGroupPolicy> fis = filter.getGroupRules();
            String mrulStr = JsonUtils.writeValueAsString(fis);
            Assert.assertEquals(sruleStr, mrulStr);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testGetGroupNameByGroupRule() {

        boolean status = true;
        try {
            String groupRuleStr =
                "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001,002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
            @SuppressWarnings("unchecked")
            List<Map<String, String>> groupRuleList = JsonUtils.readValue(groupRuleStr.getBytes(), List.class);
            List<GrayReleaseGroupPolicy> groupRules = new ArrayList<GrayReleaseGroupPolicy>();
            for (Map<String, String> ruleMap : groupRuleList) {
                GrayReleaseGroupPolicy grayReleaseGroupPolicy = new GrayReleaseGroupPolicy(
                        ruleMap.get("name"), ruleMap.get("rule"));
                groupRules.add(grayReleaseGroupPolicy);
            }
            Deencapsulation.setField(filter, "groupRules", groupRules);
            String tags = "001";
            String version = "1.0.0.1";
            String groupId = filter.getGroupNameByGroupRule(tags, version);
            String dgroupId = "001";
            Assert.assertEquals(dgroupId, groupId);

            String tags1 = "003";
            String version1 = "1.0.0.1";
            String groupId1 = filter.getGroupNameByGroupRule(tags1, version1);
            String DEFAULT_INSTANCE_GROUP = "graydefaultgroup";
            Assert.assertEquals(DEFAULT_INSTANCE_GROUP, groupId1);

        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testParseStrRule() {

        String ruleStr = "userid=00358&&aa~123*";
        Map<String, Object> desRule = filter.parseStrRule(ruleStr);
        Map<String, Object> ruleMap = new HashMap<String, Object>();
        ruleMap.put("operator", RelateSymbol.And);
        List<Map<String, String>> objectList = new ArrayList<Map<String, String>>();
        Map<String, String> object1 = new HashMap<String, String>();
        object1.put("name", "userid");
        object1.put("value", "00358");
        object1.put("operator", "Equal");
        objectList.add(object1);
        Map<String, String> object2 = new HashMap<String, String>();
        object2.put("name", "aa");
        object2.put("value", "123*");
        object2.put("operator", "Like");
        objectList.add(object2);
        ruleMap.put("objectList", objectList);
        Assert.assertEquals(ruleMap.toString(), desRule.toString());
    }

    @Test
    public void testDefineInstanceScope() {

        boolean status = true;
        try {
            String groupRuleStr =
                //                "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001;002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
                "[{\"name\":\"001\",\"rule\":\"tags=001;002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
            @SuppressWarnings("unchecked")
            List<Map<String, String>> groupRuleList = JsonUtils.readValue(groupRuleStr.getBytes(), List.class);
            List<GrayReleaseGroupPolicy> groupRules = new ArrayList<GrayReleaseGroupPolicy>();
            for (Map<String, String> ruleMap : groupRuleList) {
                GrayReleaseGroupPolicy grayReleaseGroupPolicy = new GrayReleaseGroupPolicy(
                        ruleMap.get("name"), ruleMap.get("rule"));
                groupRules.add(grayReleaseGroupPolicy);
            }
            Deencapsulation.setField(filter, "groupRules", groupRules);

            filter.defineInstanceScope();

            InstanceScope instanceScope = Deencapsulation.getField(filter, "instanceScope");
            Assert.assertEquals(InstanceScope.All, instanceScope);

            List<String> versions = Deencapsulation.getField(filter, "versions");
            System.out.println(versions);
            Assert.assertEquals(null, versions);

        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
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
