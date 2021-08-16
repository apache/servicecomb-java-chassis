package org.apache.servicecomb.darklaunch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DarklaunchRuleItemJson {
  private String groupName;

  private String groupCondition;

  private String policyCondition;

  private boolean caseInsensitive;

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getGroupCondition() {
    return groupCondition;
  }

  public void setGroupCondition(String groupCondition) {
    this.groupCondition = groupCondition;
  }

  public String getPolicyCondition() {
    return policyCondition;
  }

  public void setPolicyCondition(String policyCondition) {
    this.policyCondition = policyCondition;
  }

  public boolean isCaseInsensitive() {
    return caseInsensitive;
  }

  public void setCaseInsensitive(boolean caseInsensitive) {
    this.caseInsensitive = caseInsensitive;
  }
}
