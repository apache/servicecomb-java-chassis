package io.servicecomb.grayrelease;

public class GrayReleaseRulePolicy {

    private String groupName;

    private String type;

    private String policy;

    public GrayReleaseRulePolicy(String groupName, String type, String policy) {
        this.groupName = groupName;
        this.type = type;
        this.policy = policy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String toString() {
        StringBuffer toStr = new StringBuffer();
        toStr.append("{").append("groupName:").append(this.groupName).append(",");
        toStr.append("type:").append(this.type).append(",");
        toStr.append("policy:").append(this.policy).append("}");
        return toStr.toString();
    }

}
