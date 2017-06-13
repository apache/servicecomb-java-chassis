package io.servicecomb.grayrelease.csefilter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.grayrelease.AbstractGrayReleaseFilter;
import io.servicecomb.grayrelease.GrayReleaseGroupPolicy;
import io.servicecomb.grayrelease.GrayReleaseRulePolicy;
import io.servicecomb.loadbalance.Configuration;

public abstract class AbstractCseRuleGrayReleaseFilter extends AbstractGrayReleaseFilter {
    protected static final String RULE_DETAILS_NAME_KEY = "name";

    protected static final String RULE_DETAILS_VALUE_KEY = "value";

    protected static final String RULE_DETAILS_OBJECTLIST_NAME = "objectList";

    @Override
    protected void fillGrayRules() {
        try {
            String ruleStr = Configuration.INSTANCE.getGrayreleaseRulePolicy(invocation.getMicroserviceName(),
                    invocation.getMicroserviceQualifiedName());
            if ("".equals(ruleStr) || ruleStr == null) {
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, String>> grayRuleList =
                JsonUtils.readValue(ruleStr.getBytes(StandardCharsets.UTF_8), List.class);
            grayRules = new ArrayList<GrayReleaseRulePolicy>();
            for (Map<String, String> ruleMap : grayRuleList) {
                GrayReleaseRulePolicy grayReleaseGroupPolicy = new GrayReleaseRulePolicy(
                        ruleMap.get(GRAY_RULE_GROUP_KEY),
                        ruleMap.get(GRAY_RULE_TYPE_KEY),
                        ruleMap.get(GRAY_RULE_POLICY_KEY));
                grayRules.add(grayReleaseGroupPolicy);
            }

        } catch (Exception e) {
            LOGGER.error(this.getClass().getName() + ".fillRules error:" + e);
            grayRules = null;
        }
    }

    @Override
    protected void fillGroupRules() {
        try {
            String groupPolicy =
                Configuration.INSTANCE.getGrayreleaseInstanceGroupRule(invocation.getMicroserviceName());
            if ("".equals(groupPolicy) || groupPolicy == null) {
                return;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, String>> ruleList =
                JsonUtils.readValue(groupPolicy.getBytes(StandardCharsets.UTF_8), List.class);
            groupRules = new ArrayList<GrayReleaseGroupPolicy>();
            for (Map<String, String> ruleMap : ruleList) {
                GrayReleaseGroupPolicy grayReleaseGroupPolicy = new GrayReleaseGroupPolicy(
                        ruleMap.get(GROUP_RULE_NAME_KEY), ruleMap.get(GROUP_RULE_RULE_KEY));
                groupRules.add(grayReleaseGroupPolicy);
            }
        } catch (Exception e) {
            LOGGER.error(this.getClass().getName() + ".fillGroupRules error:" + e);
            groupRules = null;
        }

    }

    @Override
    protected String getGroupNameByGroupRule(String tags, String version) {
        String rGroup = DEFAULT_INSTANCE_GROUP;
        if (groupRules == null) {
            return DEFAULT_INSTANCE_GROUP;
        }
        for (GrayReleaseGroupPolicy insGroupRule : groupRules) {

            String groupName = insGroupRule.getGroupName();

            String rule = insGroupRule.getRule();
            Map<String, Object> groupRule = parseStrRule(rule);

            String tagRuleValue = null;
            String versionRuleValue = null;
            RelateSymbol operator;
            if (groupRule == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ruleDetailList =
                (List<Map<String, Object>>) groupRule.get(RULE_DETAILS_OBJECTLIST_NAME);
            if (ruleDetailList == null || ruleDetailList.size() == 0) {
                continue;
            }
            for (Map<String, Object> ruleDetail : ruleDetailList) {
                if (INSTANCE_GROUP_VERSION_KEY.equals(ruleDetail.get(RULE_DETAILS_NAME_KEY))) {
                    versionRuleValue = ruleDetail.get(RULE_DETAILS_VALUE_KEY) == null ? DEFAULT_MICROSERVICE_VERSION
                            : (String) ruleDetail.get(RULE_DETAILS_VALUE_KEY);
                }
                if (INSTANCE_GROUP_TAG_KEY.equals(ruleDetail.get(RULE_DETAILS_NAME_KEY))) {
                    tagRuleValue = ruleDetail.get(RULE_DETAILS_VALUE_KEY) == null ? NULL_STR
                            : (String) ruleDetail.get(RULE_DETAILS_VALUE_KEY);
                }
            }
            if (tagRuleValue == null) {
                tagRuleValue = NULL_STR;
            }
            if (versionRuleValue == null) {
                versionRuleValue = DEFAULT_MICROSERVICE_VERSION;
            }

            if (NULL_STR.equals(tagRuleValue) && DEFAULT_MICROSERVICE_VERSION.equals(versionRuleValue)) {
                continue;
            }

            if (NULL_STR.equals(tagRuleValue)) {
                if (version.equals(versionRuleValue)) {
                    rGroup = groupName;
                    break;
                } else {
                    continue;
                }
            } else if (DEFAULT_MICROSERVICE_VERSION.equals(versionRuleValue)) {
                String[] tagsRuleArray = tagRuleValue.split(",");
                for (String tagRule : tagsRuleArray) {
                    if (tags.contains(tagRule)) {
                        rGroup = groupName;
                        break;
                    }
                }
            } else {
                operator = (RelateSymbol) groupRule.get(OPERATOR_NAME);
                if (operator == null) {
                    operator = RelateSymbol.And;
                }
                boolean isTagCompare = false;
                String[] tagsRuleArray = tagRuleValue.split(",");
                for (String tagRule : tagsRuleArray) {
                    if (tags.contains(tagRule)) {
                        isTagCompare = true;
                        break;
                    }
                }
                if (operator == RelateSymbol.And) {
                    if ((isTagCompare)
                            && (version.equals(versionRuleValue))) {
                        rGroup = groupName;
                        break;
                    }
                } else if (operator == RelateSymbol.Or) {
                    if ((isTagCompare)
                            || version.equals(versionRuleValue)) {
                        rGroup = groupName;
                        break;
                    }
                }
            }

        }
        return rGroup;
    }

    protected enum RelateSymbol {
        And,
        Or
    }

    protected enum LogicSymbol {
        Larger,
        Smaller,
        Equal,
        Like,
        NotEqual,
        LargerOrEqual,
        SmallerOrEqual
    }

    protected Map<String, Object> parseStrRule(String ruleStr) {
        Map<String, Object> ruleMap = new HashMap<String, Object>();
        String[] ruleSubStr = null;
        if (ruleStr.contains("||")) {
            ruleSubStr = ruleStr.split("\\|\\|");
            ruleMap.put(OPERATOR_NAME, RelateSymbol.Or);
        } else if (ruleStr.contains("&&")) {
            ruleSubStr = ruleStr.split("&&");
            ruleMap.put(OPERATOR_NAME, RelateSymbol.And);
        }
        if (ruleSubStr == null) {
            ruleSubStr = new String[1];
            ruleSubStr[0] = ruleStr;
        }

        List<Map<String, Object>> objectList = new ArrayList<Map<String, Object>>();

        for (String subRuleStr : ruleSubStr) {
            Map<String, Object> newSubRule = new HashMap<String, Object>();
            String logicSymbol = "=";
            if (subRuleStr.contains(">")) {
                newSubRule.put(OPERATOR_NAME, LogicSymbol.Larger);
                logicSymbol = ">";
            } else if (subRuleStr.contains("<")) {
                newSubRule.put(OPERATOR_NAME, LogicSymbol.Smaller);
                logicSymbol = "<";
            } else if (subRuleStr.contains("<=")) {
                newSubRule.put(OPERATOR_NAME, LogicSymbol.SmallerOrEqual);
                logicSymbol = "<=";
            } else if (subRuleStr.contains(">=")) {
                newSubRule.put(OPERATOR_NAME, LogicSymbol.LargerOrEqual);
                logicSymbol = ">=";
            } else if (subRuleStr.contains("=")) {
                newSubRule.put(OPERATOR_NAME, LogicSymbol.Equal);
                logicSymbol = "=";
            } else if (subRuleStr.contains("~")) {
                newSubRule.put(OPERATOR_NAME, LogicSymbol.Like);
                logicSymbol = "~";
            } else if (subRuleStr.contains("!=")) {
                newSubRule.put(OPERATOR_NAME, LogicSymbol.NotEqual);
                logicSymbol = "!=";
            }

            String[] subStrs = subRuleStr.split(logicSymbol);
            newSubRule.put(RULE_DETAILS_NAME_KEY, subStrs[0]);
            newSubRule.put(RULE_DETAILS_VALUE_KEY, subStrs[1]);
            objectList.add(newSubRule);
        }
        ruleMap.put(RULE_DETAILS_OBJECTLIST_NAME, objectList);
        return ruleMap;
    }

    @Override
    protected void defineInstanceScope() {

        int groupRuleSize = groupRules.size();
        List<String> versions = new ArrayList<String>();
        for (GrayReleaseGroupPolicy insGroupRule : groupRules) {
            String ruleStr = insGroupRule.getRule();
            Map<String, Object> groupRule = parseStrRule(ruleStr);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ruleDetailList =
                (List<Map<String, Object>>) groupRule.get(RULE_DETAILS_OBJECTLIST_NAME);

            String versionValue = DEFAULT_MICROSERVICE_VERSION;
            for (Map<String, Object> ruleDetail : ruleDetailList) {
                if (INSTANCE_GROUP_VERSION_KEY.equals(ruleDetail.get(RULE_DETAILS_NAME_KEY))) {
                    versionValue = ruleDetail.get(RULE_DETAILS_VALUE_KEY) == null ? DEFAULT_MICROSERVICE_VERSION
                            : (String) ruleDetail.get(RULE_DETAILS_VALUE_KEY);
                }

            }
            if (!DEFAULT_MICROSERVICE_VERSION.equals(versionValue)) {
                versions.add(versionValue);
            }
        }
        int versionRuleCount = versions.size();
        if (versionRuleCount == 0) {
            setInstanceScope(InstanceScope.VersionRule);
        } else if (versionRuleCount < groupRuleSize) {
            setInstanceScope(InstanceScope.All);
        } else if (versionRuleCount == groupRuleSize) {
            setInstanceScope(InstanceScope.PartVersion);
            setVersions(versions);
        }
    }
}
