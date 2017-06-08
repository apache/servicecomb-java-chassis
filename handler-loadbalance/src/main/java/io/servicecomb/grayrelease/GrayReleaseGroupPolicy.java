package io.servicecomb.grayrelease;

public class GrayReleaseGroupPolicy {

    private String groupName;

    private String rule;

    public GrayReleaseGroupPolicy(String groupName, String rule) {
        this.groupName = groupName;
        this.rule = rule;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

}
