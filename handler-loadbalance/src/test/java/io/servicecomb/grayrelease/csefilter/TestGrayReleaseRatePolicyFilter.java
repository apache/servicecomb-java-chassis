package io.servicecomb.grayrelease.csefilter;

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
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestGrayReleaseRatePolicyFilter {

    private GrayReleaseRatePolicyFilter filter = Mockito.mock(GrayReleaseRatePolicyFilter.class,
            Mockito.CALLS_REAL_METHODS);

    private Invocation invocation = Mockito.mock(Invocation.class);

    {
        this.initInvocation();
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
        Mockito.when(filter.getInvocation()).thenReturn(invocation);
        Deencapsulation.setField(filter, "invocation", invocation);

    }

    @Test
    public void testFillRules() {
        boolean status = true;
        try {
            String grayReleaseRuleClassName = "com.huawei.paas.cse.grayrelease.csefilter.GrayReleaseRatePolicyFilter";
            String rulePolicy = "[{\"group\":\"001\",\"type\":\"rate\",\"policy\":\"20\"}]";
            String groupPolicy =
                "[{\"name\":\"001\",\"rule\":\"version=1.0.0.1&&tags=001;002\"},{\"name\":\"002\",\"rule\":\"version=1.0.0.2&&tags=002\"}]";
            changeConfig(grayReleaseRuleClassName,
                    rulePolicy,
                    groupPolicy);
            String descRuleStr =
                "[{groupName:001,type:rate,policy:20}, {groupName:graydefaultgroup,type:rate,policy:80}]";
            filter.fillGrayRules();
            Assert.assertEquals(descRuleStr, filter.getGrayRules().toString());
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testCreateInstanceChooser() {

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
            Deencapsulation.invoke(filter, "createInstanceChooser");
            Map<String, String> chooser = Deencapsulation.getField(filter, "instanceChooser");
            Map<String, String> desChooser = new HashMap<String, String>();
            desChooser.put(
                    "||21||22||23||24||25||26||27||28||29||30||31||32||33||34||35||36||37||38||39||40||41||42||43||44||45||46||47||48||49||50||51||52||53||54||55||56||57||58||59||60||61||62||63||64||65||66||67||68||69||70||71||72||73||74||75||76||77||78||79||80||81||82||83||84||85||86||87||88||89||90||91||92||93||94||95||96||97||98||99||100",
                    "graydefaultgroup");
            desChooser.put(
                    "||1||2||3||4||5||6||7||8||9||10||11||12||13||14||15||16||17||18||19||20",
                    "001");
            Assert.assertEquals(desChooser, chooser);
        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);

    }

    @Test
    public void testChooseCompute() {

        boolean status = false;
        try {

            Map<String, String> desChooser = new HashMap<String, String>();
            desChooser.put(
                    "||21||22||23||24||25||26||27||28||29||30||31||32||33||34||35||36||37||38||39||40||41||42||43||44||45||46||47||48||49||50||51||52||53||54||55||56||57||58||59||60||61||62||63||64||65||66||67||68||69||70||71||72||73||74||75||76||77||78||79||80||81||82||83||84||85||86||87||88||89||90||91||92||93||94||95||96||97||98||99||100",
                    "graydefaultgroup");
            desChooser.put(
                    "||1||2||3||4||5||6||7||8||9||10||11||12||13||14||15||16||17||18||19||20",
                    "001");

            Deencapsulation.setField(filter, "instanceChooser", desChooser);
            String groupId = Deencapsulation.invoke(filter, "chooseCompute");
            if ("001".equals(groupId) || "graydefaultgroup".equals(groupId)) {
                status = true;
            }

        } catch (Exception e) {
            status = false;
        }
        Assert.assertTrue(status);
    }

    @Test
    public void testGrayChooseForGroupIdByRules() {
        new MockUp<GrayReleaseRatePolicyFilter>() {
            @Mock
            private void createInstanceChooser() {
            }

            @Mock
            private String chooseCompute() {
                return "001";
            }
        };
        String gId = filter.grayChooseForGroupIdByRules();
        Assert.assertEquals("001", gId);
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
