package io.servicecomb.grayrelease.csefilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.grayrelease.GrayReleaseRulePolicy;
import io.servicecomb.grayrelease.csefilter.AbstractCseRuleGrayReleaseFilter.LogicSymbol;
import mockit.Deencapsulation;

public class TestGrayReleaseRulePolicyFilter {
    private GrayReleaseRulePolicyFilter filter = Mockito.mock(GrayReleaseRulePolicyFilter.class,
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
    public void testGrayChooseForGroupIdByRules() {

        boolean status = true;
        try {

            String grayRulesStr =
                "[{\"group\":\"0001\",\"type\":\"rule\",\"policy\":\"name=hello||aa=123\"}]";
            @SuppressWarnings("unchecked")
            List<Map<String, String>> grayRuleList = JsonUtils.readValue(grayRulesStr.getBytes(), List.class);
            List<GrayReleaseRulePolicy> grayRules = new ArrayList<GrayReleaseRulePolicy>();
            for (Map<String, String> ruleMap : grayRuleList) {
                GrayReleaseRulePolicy grayReleaseGroupPolicy = new GrayReleaseRulePolicy(
                        ruleMap.get("group"), ruleMap.get("type"), ruleMap.get("policy"));
                grayRules.add(grayReleaseGroupPolicy);
            }
            Deencapsulation.setField(filter, "grayRules", grayRules);

            Map<String, Object> reqParams1 = new HashMap<String, Object>();
            reqParams1.put("name", "hello");
            Deencapsulation.setField(filter, "reqParams", reqParams1);
            Assert.assertEquals("0001", filter.grayChooseForGroupIdByRules());

            Map<String, Object> reqParams2 = new HashMap<String, Object>();
            reqParams2.put("name", "world");
            Deencapsulation.setField(filter, "reqParams", reqParams2);
            Assert.assertEquals(null, filter.grayChooseForGroupIdByRules());

        } catch (Exception e) {
            status = false;

        }
        Assert.assertTrue(status);
    }

    @Test
    public void testPramsCompare() {

        Map<String, Object> reqParams1 = new HashMap<String, Object>();
        reqParams1.put("name", "hello");

        Deencapsulation.setField(filter, "reqParams", reqParams1);
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "pramsCompare", "name", LogicSymbol.Equal, "hello"));
        Assert.assertEquals(false,
                Deencapsulation.invoke(filter, "pramsCompare", "name", LogicSymbol.Equal, "world"));
        Assert.assertEquals(false, Deencapsulation.invoke(filter, "pramsCompare", "name", LogicSymbol.Like, "wor*"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "pramsCompare", "name", LogicSymbol.Like, "he*"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "pramsCompare", "name", LogicSymbol.Like, "he*lo"));

        Map<String, Object> reqParams2 = new HashMap<String, Object>();
        reqParams2.put("name", "1000");
        Deencapsulation.setField(filter, "reqParams", reqParams2);
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "pramsCompare", "name", LogicSymbol.Larger, "500"));
        Assert.assertEquals(false,
                Deencapsulation.invoke(filter, "pramsCompare", "name", LogicSymbol.Smaller, "500"));
    }

    @Test
    public void testCompare() {

        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "1000", LogicSymbol.Larger, "900"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "900", LogicSymbol.LargerOrEqual, "900"));
        Assert.assertEquals(false,
                Deencapsulation.invoke(filter, "compare", "900", LogicSymbol.LargerOrEqual, "1000"));
        Assert.assertEquals(false, Deencapsulation.invoke(filter, "compare", "900", LogicSymbol.Larger, "1000"));
        Assert.assertEquals(false, Deencapsulation.invoke(filter, "compare", "1000", LogicSymbol.Smaller, "900"));
        Assert.assertEquals(true,
                Deencapsulation.invoke(filter, "compare", "900", LogicSymbol.SmallerOrEqual, "900"));
        Assert.assertEquals(true,
                Deencapsulation.invoke(filter, "compare", "900", LogicSymbol.SmallerOrEqual, "1000"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "900", LogicSymbol.Smaller, "1000"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "world", LogicSymbol.Equal, "world"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "20", LogicSymbol.Equal, "20"));
        Assert.assertEquals(false, Deencapsulation.invoke(filter, "compare", "world", LogicSymbol.Equal, "hello"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "world", LogicSymbol.Like, "w*"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "world", LogicSymbol.Like, "*"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "world", LogicSymbol.Like, "w*d"));
        Assert.assertEquals(false, Deencapsulation.invoke(filter, "compare", "world", LogicSymbol.Like, "w*l"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "world", LogicSymbol.Like, "w??l?"));
        Assert.assertEquals(true, Deencapsulation.invoke(filter, "compare", "world", LogicSymbol.Like, "w*l?"));

    }

}
