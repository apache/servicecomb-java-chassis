package org.apache.servicecomb.darklaunch;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DarklaunchRuleJson {
  private PolicyType policyType;

  private List<DarklaunchRuleItemJson> ruleItems = new ArrayList<>();

  public PolicyType getPolicyType() {
    return policyType;
  }

  public void setPolicyType(PolicyType policyType) {
    this.policyType = policyType;
  }

  public List<DarklaunchRuleItemJson> getRuleItems() {
    return ruleItems;
  }

  public void setRuleItems(List<DarklaunchRuleItemJson> ruleItems) {
    this.ruleItems = ruleItems;
  }

  public void addRuleItem(DarklaunchRuleItemJson ruleItem) {
    this.ruleItems.add(ruleItem);
  }
}
