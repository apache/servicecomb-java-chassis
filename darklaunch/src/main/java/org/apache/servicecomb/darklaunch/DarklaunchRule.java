package org.apache.servicecomb.darklaunch;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.darklaunch.oper.ConditionFactory;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DarklaunchRule {
  private static final Logger LOG = LoggerFactory.getLogger(DarklaunchRule.class);

  public static final String PROP_VERSION = "version";

  public static final String PROP_TAG = "tag";

  public static final String PROP_PERCENT = "rate";

  private PolicyType policyType;

  private List<DarklaunchRuleItem> ruleItems = new ArrayList<>();

  public static DarklaunchRule parse(String ruleStr) {
    if (StringUtils.isEmpty(ruleStr)) {
      return null;
    }

    try {
      DarklaunchRuleJson ruleJson = JsonUtils
          .readValue(ruleStr.getBytes(Charset.forName("UTF-8")), DarklaunchRuleJson.class);
      DarklaunchRule rule = new DarklaunchRule(ruleJson.getPolicyType());
      for (DarklaunchRuleItemJson itemJson : ruleJson.getRuleItems()) {
        DarklaunchRuleItem item = new DarklaunchRuleItem(itemJson.getGroupName());
        item.setGroupCondition(
            ConditionFactory.buildCondition(itemJson.getGroupCondition(), itemJson.isCaseInsensitive())
        );
        if (rule.getPolicyType().equals(PolicyType.RULE)) {
          item.setPolicyCondition(
              ConditionFactory.buildCondition(itemJson.getPolicyCondition(), itemJson.isCaseInsensitive()));
        } else {
          item.setPolicyCondition(ConditionFactory.buildRateCondition(itemJson.getPolicyCondition()));
        }
        rule.addRuleItem(item);
      }
      return rule;
    } catch (Exception e) {
      LOG.warn("Invalid configuration: rule={},message={}", ruleStr, e.getMessage());
    }
    return null;
  }

  public DarklaunchRule(PolicyType policyType) {
    this.policyType = policyType;
  }

  public PolicyType getPolicyType() {
    return policyType;
  }

  public void setPolicyType(PolicyType policyType) {
    this.policyType = policyType;
  }

  public List<DarklaunchRuleItem> getRuleItems() {
    return ruleItems;
  }

  public void addRuleItem(DarklaunchRuleItem ruleItem) {
    this.ruleItems.add(ruleItem);
  }
}

